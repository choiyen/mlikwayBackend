package project.MilkyWay.noticeMain.Common.Controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import project.MilkyWay.ComonType.DTO.PageDTO;
import project.MilkyWay.ComonType.DTO.ResponseDTO;
import project.MilkyWay.ComonType.Enum.CleanType;
import project.MilkyWay.ComonType.Expection.*;
import project.MilkyWay.ComonType.LoginSuccess;
import project.MilkyWay.Config.SessionManager;
import project.MilkyWay.S3ClientService.MultiS3ImageService;
import project.MilkyWay.S3ClientService.S3ImageService;
import project.MilkyWay.noticeMain.Common.DTO.NoticeJsonDTO;
import project.MilkyWay.noticeMain.Common.DTO.TypeDTO;
import project.MilkyWay.noticeMain.Notice.DTO.NoticeDTO;
import project.MilkyWay.noticeMain.NoticeDetail.DTO.NoticeDetailDTO;
import project.MilkyWay.noticeMain.NoticeDetail.Entity.NoticeDetailEntity;
import project.MilkyWay.noticeMain.Notice.Entity.NoticeEntity;
import project.MilkyWay.noticeMain.NoticeDetail.Service.MultiNoticeDetailService;
import project.MilkyWay.noticeMain.NoticeDetail.Service.NoticeDetailService;
import project.MilkyWay.noticeMain.Notice.Service.NoticeService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/notice")
@Tag(name = "전체 Notice 정보를 제공하는 Controller")
public class NoticeController //Notice, Noticedetaill 동시 동작
{
    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeDetailService noticeDetailService;


    private final ResponseDTO<Object> responseDTO = new ResponseDTO<>();

    @Autowired
    private S3ImageService s3ImageService;

    @Autowired
    private MultiNoticeDetailService multiNoticeDetailService;

    @Autowired
    private MultiS3ImageService multiS3ImageService;

    LoginSuccess loginSuccess = new LoginSuccess();

    //Node jS에서도 사용 가능하니까, 나중에 플젝 정리할 때 추가 체크하자.
    @Operation(
            summary =  "Create a new NoticeEntity and NoticeDetail , but only if the user is an administrator.",
            description = "This API creates a new NoticeEntity and NoticeDetail and returns NoticeDTO and NoticeDetailDTO  as response",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Notice and NoticeDetail created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(oneOf = {NoticeDTO.class, NoticeDetailDTO.class})
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> Insert(HttpServletRequest request,
                                    @RequestPart("noticeJsonDTO") String noticeJsonJson,
                                    @RequestPart("titleimg") MultipartFile titleimg,
                                    MultipartHttpServletRequest multiRequest) throws IOException { // after_0, after_1...
        try
        {

            ObjectMapper objectMapper = new ObjectMapper();
            NoticeJsonDTO noticeJsonDTO = objectMapper.readValue(noticeJsonJson, NoticeJsonDTO.class);

            if(loginSuccess.isSessionExist(request))
            {

                NoticeDTO notice1 = noticeService.InsertNotice(noticeJsonDTO.getNoticeDTO(), titleimg);
                if(notice1 != null)
                {
                    int i = 0;
                    List<NoticeDetailDTO> noticeDetailEntities = new ArrayList<>();

                    // 파일 처리 — key 별로 파일 리스트 얻기
                    Iterator<String> fileNames = multiRequest.getFileNames();
                    Map<String, List<String>> uploadedFileUrls = new HashMap<>();
                    Map<String, CompletableFuture<List<String>>> futureMap = new HashMap<>();
                    while (fileNames.hasNext()) {
                        String key = fileNames.next();
                        List<MultipartFile> files = multiRequest.getFiles(key);
                        CompletableFuture<List<String>> sData = multiS3ImageService.S3Import(files);
                        futureMap.put(key, sData);
                    }

                    for (Map.Entry<String, CompletableFuture<List<String>>> entry : futureMap.entrySet()) {
                        try {
                            uploadedFileUrls.put(entry.getKey(), entry.getValue().get()); // 기다림은 여기서만
                        } catch (Exception e) {
                            e.printStackTrace(); // 적절한 예외 처리
                        }
                    }
                    noticeDetailEntities.addAll(multiNoticeDetailService.S3Import(notice1.getNoticeId(), noticeJsonDTO.getNoticeDetailDTO(), uploadedFileUrls).get());
                    List<Object> list = new ArrayList<>();
                    list.add(notice1);
                    list.add(noticeDetailEntities);
                    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO.Response("success","후기 데이터 등록에 성공했습니다.", list));

                }
                else
                {
                    throw new InsertFailedException("데이터 저장에 실패했습니다.");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 후기 정보 등록은 관리자 로그인이 필요합니다.");
            }
        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error",e.getMessage()));
        }
    }

    @Operation(
            summary =  "Change a NoticeDTO and NoticeDetailDTO List by NoticeId , but only if the user is an administrator.",  // Provide a brief summary
            description = "This API Change a NoticeDTO, NoticeDetailDTO and returns NoticeJsonDTO as response",  // Provide detailed description
            responses = {
                    @ApiResponse(responseCode = "201", description = "notice and NoticeDetail Changed successfully", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {NoticeDTO.class, NoticeDetailDTO.class} ))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid Change data"
                    )
            }
    )
    @PutMapping
    public ResponseEntity<?> Update(HttpServletRequest request,
                                    @RequestPart("noticeJsonDTO") String noticeJsonJson,
                                    @RequestPart("titleimg") MultipartFile titleimg,
                                    MultipartHttpServletRequest multiRequest) throws IOException
    {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            NoticeJsonDTO noticeJsonDTO = objectMapper.readValue(noticeJsonJson, NoticeJsonDTO.class);
            NoticeDTO oldnotice = noticeService.findNoticeId(noticeJsonDTO.getNoticeDTO().getNoticeId());
            if(loginSuccess.isSessionExist(request))
            {
                NoticeDTO notice1 = noticeService.UpdateNotice(noticeJsonDTO.getNoticeDTO(),titleimg);
                if(notice1 != null)
                {
                    int i = 0;
                    List<NoticeDetailDTO> noticeDetailEntities = new ArrayList<>();
                    Set<Number> excludeIds = new HashSet<>();  // ArrayList를 Set으로 변환

                    // 파일 처리 — key 별로 파일 리스트 얻기
                    Iterator<String> fileNames = multiRequest.getFileNames();
                    Map<String, List<String>> uploadedFileUrls = new HashMap<>();

                    while (fileNames.hasNext()) {
                        String key = fileNames.next(); //before_1, affer_1
                        String[] parts = key.split("_"); // "before", "1"
                        if (parts.length != 2) continue; // 예외 처리
                        String type = parts[0];

                        // 슨서
                        int index = Integer.parseInt(parts[1]);
                        List<MultipartFile> files = multiRequest.getFiles(key);
                        List<String> urlList = new ArrayList<>();

                        for (int fileIndex = 0; fileIndex < files.size(); fileIndex++) {
                            MultipartFile file = files.get(fileIndex);
                            String filename = file.getOriginalFilename();

                            if (filename == null || !filename.contains(".")) {
                                throw new RuntimeException("No file extension found");
                            }

                            try
                            {
                                NoticeDetailDTO noticeDetailDTO = noticeDetailService.noticeDetail(noticeJsonDTO.getNoticeDetailDTO().get(index).getNoticeDetailId());
                                if (file != null && !file.isEmpty())
                                {
                                    if(type.equals("before"))
                                    {
                                        if(noticeDetailDTO.getBeforeURL().get(fileIndex) != null)
                                        {
                                            System.out.println("Delete Image : " + noticeDetailDTO.getBeforeURL().get(fileIndex));
                                            FileDelete(noticeDetailDTO.getBeforeURL().get(fileIndex));
                                        }
                                    }
                                    else if(type.equals("after"))
                                    {
                                        if(noticeDetailDTO.getBeforeURL().get(fileIndex) != null) {
                                            System.out.println("Delete Image : " + noticeDetailDTO.getAfterURL().get(fileIndex));
                                            FileDelete(noticeDetailDTO.getAfterURL().get(fileIndex));
                                        }
                                    }
                                    urlList.add(uploading(file));
                                }
                                else
                                {
                                    String existUrl = null;
                                    if (type.equals("before"))
                                    {
                                        existUrl = noticeJsonDTO.getNoticeDetailDTO().get(index).getBeforeURL().get(fileIndex);
                                    } else if (type.equals("after")) {
                                        existUrl = noticeJsonDTO.getNoticeDetailDTO().get(index).getAfterURL().get(fileIndex);
                                    }
                                    urlList.add(existUrl);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                throw new RuntimeException("업로드 실패");
                            }
                        }

                        uploadedFileUrls.put(key, urlList);
                    }


                    while (i < noticeJsonDTO.getNoticeDetailDTO().size())
                    {
                        List<String> beforeUrls = uploadedFileUrls.getOrDefault("before_" + i, new ArrayList<>());
                        List<String> afterUrls  = uploadedFileUrls.getOrDefault("after_" + i, new ArrayList<>());
                        if(noticeJsonDTO.getNoticeDetailDTO().get(i).getNoticeDetailId() != null )
                        {
                            NoticeDetailDTO noticeDetailDTO1 = noticeDetailService.UpdateNoticeDetailMapper(noticeJsonDTO.getNoticeDetailDTO().get(i), beforeUrls,afterUrls);
                            if(noticeDetailDTO1 != null)
                            {
                                noticeDetailEntities.add(noticeDetailDTO1);
                                excludeIds.add(noticeDetailDTO1.getNoticeDetailId());
                            }
                            else
                            {
                                noticeService.UpdateNotice(oldnotice, null);
                                throw new InsertFailedException("데이터 저장에 실패했습니다.");
                            }
                        }
                        else
                        {
                            NoticeDetailDTO noticeDetailDTO = noticeDetailService.InsertNoticeDetallMapper(noticeJsonDTO.getNoticeDetailDTO().get(i),noticeJsonDTO.getNoticeDTO().getNoticeId(), beforeUrls, afterUrls);
                            if(noticeDetailDTO != null)
                            {
                                noticeDetailEntities.add(noticeDetailDTO);
                                excludeIds.add(noticeDetailDTO.getNoticeDetailId());
                            }
                            else
                            {
                                noticeService.UpdateNotice(oldnotice,null);
                                throw new InsertFailedException("데이터 저장에 실패했습니다.");
                            }
                        }
                        i++;
                    }
                    List<Object> list = new ArrayList<>();
                    list.add(notice1);
                    list.add(noticeDetailEntities);
                    List<NoticeDetailEntity> noticeDetailEntity = noticeDetailService.ListNoticeDetail(notice1.getNoticeId())
                            .stream()
                            .filter(entity -> !excludeIds.contains(entity.getNoticeDetailId()))  // 제외 조건
                        .toList();  // 필터링 후 새로운 리스트 생성;
                    boolean bool = true;
                    for(NoticeDetailEntity noticeDetail : noticeDetailEntity)
                    {
                       bool= noticeDetailService.DeleteToNoticeDetail(noticeDetail.getNoticeDetailId());
                    }

                    if(bool)
                    {
                        return ResponseEntity.ok().body(responseDTO.Response("success","후기 데이터 수정에 성공했습니다.", list));
                    }
                    else
                    {
                        throw new RuntimeException("알 수 없는 런타임 오류가 발생하였습니다. 다시 시도해주세요");
                    }

                }
                else
                {
                    throw new UpdateFailedException("후기 데이터 수정에 실패하였습니다.");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 후기 정보의 수정은 관리자에게만 허용됩니다.");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Delete an notice and NoticeDetail List by noticeId , but only if the user is an administrator.",  // Provide a brief summary
            description = "This API deletes an notice and NoticeDetail List by the provided noticeId and returns a ResponseEntity with a success or failure message.",  // Provide detailed description
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "notice and NoticeDetail deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "notice and NoticeDetail not found"
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<?> Delete(HttpServletRequest request, @RequestParam String noticeId)
    {
        try
        {
            if(loginSuccess.isSessionExist(request))
            {
                List<NoticeDetailEntity> noticeDetailEntity = noticeDetailService.ListNoticeDetail(noticeId);
                if(noticeDetailEntity != null)
                {
                    for(NoticeDetailEntity noticeDetail : noticeDetailEntity)
                    {
                        noticeDetailService.DeleteToNoticeDetail(noticeDetail.getNoticeDetailId());

                    }
                    NoticeDTO noticeDTO = noticeService.findNoticeId(noticeId);
                    if(noticeDTO != null)
                    {
                        boolean bool =  noticeService.DeleteByNoticeId(noticeId);
                        if(!bool)
                        {
                            throw new DeleteFailedException("데이터 삭제에 실패했습니다. 다시 시도해주세요");
                        }
                        else
                        {
                            return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 삭제!"));
                        }
                    }
                    else
                    {
                        throw new DeleteFailedException("데이터를 지우는데 실패했습니다. 다시 시도해주세요");
                    }
                }
                else
                {
                    throw new FindFailedException("삭제할 세부 정보를 찾을 수 없습니다.");
                }
            }
            else
            {
                throw new SessionNotFoundExpection("관리자 로그인 X, 후기 정보 삭제에는 관리자 로그인이 반드시 필요합니다.");
            }


        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Returns a list of Notice objects along with their associated NoticeDetails.",
            description = "This API fetches a list of Notice and NoticeDetail objects from the database.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notice and Notice Detail List Found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeJsonDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Notice and Notice Detail List not found")
            }
    )
    @PostMapping("/search")
    public ResponseEntity<?> FindALl()
    {
        try
        {
            List<Object> list = new ArrayList<>();
            List<NoticeDTO> notice = new ArrayList<>(noticeService.findAll());
            if(notice != null)
            {

                for(NoticeDTO noticeDTO : notice)
                {
                    list.add(noticeDTO);//자동으로 못가져오면 추가하거나 수정 예정
                }
                return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 전송 완료",  list));
            }
            else
            {
                throw new FindFailedException("전체 후기 데이터를 찾아내는데 실패했습니다.");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));

        }
    }

    @Operation(
            summary = "Returns a list of Notice objects along with their associated NoticeDetails.",
            description = "This API fetches a list of Notice and NoticeDetail objects from the database.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notice and Notice Detail List Found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeJsonDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Notice and Notice Detail List not found")
            }
    )
    @GetMapping("/search/page")
    public ResponseEntity<?> findAll2(@RequestParam(name = "page", defaultValue = "0") long page)
    {
        try
        {
            List<Object> list = new ArrayList<>();
            List<NoticeDTO> notice = new ArrayList<>(noticeService.findAll2(page));
            if(notice != null)
            {

                for(NoticeDTO noticeDTO : notice)
                {
                    list.add(noticeDTO);//자동으로 못가져오면 추가하거나 수정 예정
                }
                PageDTO pageDTO = PageDTO.builder()
                        .list(list)
                        .PageCount(noticeService.totalPaging())
                        .Total(noticeService.totalRecord())
                        .build();
                return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 전송 완료",  pageDTO));
            }
            else
            {
                throw new FindFailedException("전체 후기 데이터를 찾아내는데 실패했습니다.");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));

        }
    }

    @Operation(
            summary = "Returns a list of Notice objects along with their associated NoticeDetails.",
            description = "This API fetches a list of Notice and NoticeDetail objects from the database.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notice and Notice Detail List Found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeJsonDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Notice and Notice Detail List not found")
            }
    )
    @GetMapping("/search/Type")
    public ResponseEntity<?> FindSmallALl(@RequestParam(name = "type") String type, @RequestParam(name = "page", defaultValue = "0") long page)
    {
        try
        {

            List<Object> list = new ArrayList<>();
            List<NoticeDTO> notice = new ArrayList<>(noticeService.findSmallAll(CleanType.valueOf(type),page));
            if(notice != null)
            {
                PageDTO pageDTO = PageDTO.builder()
                        .list(Collections.singletonList(notice))
                        .PageCount(noticeService.totalPaging())
                        .Total(noticeService.totalRecord())
                        .build();
                if(notice.isEmpty() == true)
                {
                    return ResponseEntity.ok().body(responseDTO.Response("empty", "자료 조사 결과 비어있는 항목입니다.",pageDTO));
                }
                return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 전송 완료",  pageDTO));
            }
            else
            {
                throw new FindFailedException("전체 후기 데이터를 찾아내는데 실패했습니다.");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));

        }
    }




    @Operation(
            summary = "Returns a NoticeDTO object for a given notice ID, along with its associated NoticeDetail list. ",
            description = "This API retrieves a notice based on the provided notice ID and returns the corresponding NoticeDTO along with its associated NoticeDetail list.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "NoticeDTO and NoticeDetail List found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeJsonDTO.class))),
                    @ApiResponse(responseCode = "404", description = "NoticeDTO and NoticeDetail List not found")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> FindByNoticeId(@RequestParam String NoticeId)
    {
        try
        {

            List<Object> list = new ArrayList<>();
            NoticeDTO notice = noticeService.findNoticeId(NoticeId);
            if(notice != null)
            {
                list.add(NoticeEntity.builder()
                                .type(notice.getType())
                                .noticeId(notice.getNoticeId())
                                .greeting(notice.getGreeting())
                                .title(notice.getTitle())
                                .titleimg(notice.getTitleimg())
                                .noticeDetailEntities(noticeDetailService.ListNoticeDetail(notice.getNoticeId()))
                        .build());
                return ResponseEntity.ok().body(responseDTO.Response("success", "데이터 전송 완료",  list));
            }
            else
            {
                throw new FindFailedException("전체 후기 데이터를 찾아내는데 실패했습니다.");
            }

        }
        catch (Exception e)
        {
            return ResponseEntity.badRequest().body(responseDTO.Response("error", e.getMessage()));

        }
    }  public String uploading(MultipartFile titleimg)
{

    try {
        return s3ImageService.upload(titleimg);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
    public void FileDelete(String url)
    {
        try {
            s3ImageService.deleteImageFromS3(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
