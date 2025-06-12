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
import project.MilkyWay.S3ClientService.S3ImageService;
import project.MilkyWay.noticeMain.Common.DTO.NoticeJsonDTO;
import project.MilkyWay.noticeMain.Common.DTO.TypeDTO;
import project.MilkyWay.noticeMain.Notice.DTO.NoticeDTO;
import project.MilkyWay.noticeMain.NoticeDetail.DTO.NoticeDetailDTO;
import project.MilkyWay.noticeMain.NoticeDetail.Entity.NoticeDetailEntity;
import project.MilkyWay.noticeMain.Notice.Entity.NoticeEntity;
import project.MilkyWay.noticeMain.NoticeDetail.Service.NoticeDetailService;
import project.MilkyWay.noticeMain.Notice.Service.NoticeService;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/notice")
@Tag(name = "전체 Notice 정보를 제공하는 Controller")
public class NoticeController //Notice, Noticedetaill 동시 동작
{
    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeDetailService noticeDetailService;


    @Autowired
    private S3ImageService s3ImageService;

    private final ResponseDTO<Object> responseDTO = new ResponseDTO<>();

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
                String uniqueId;
                LoginSuccess loginSuccess = new LoginSuccess();
                do
                {
                    uniqueId = loginSuccess.generateRandomId(15);
                    NoticeEntity notice1 = noticeService.findNoticeId(uniqueId);
                    if(notice1 == null)
                    {
                        break;
                    }
                }while (true);


                String url = uploading(titleimg);
                NoticeEntity noticeEntity = ConvertToNotice(noticeJsonDTO.getNoticeDTO(), uniqueId, url);

                NoticeEntity notice1 = noticeService.InsertNotice(noticeEntity);
                if(notice1 != null)
                {
                    int i = 0;
                    List<NoticeDetailEntity> noticeDetailEntities = new ArrayList<>();


                    // 파일 처리 — key 별로 파일 리스트 얻기
                    Iterator<String> fileNames = multiRequest.getFileNames();
                    Map<String, List<String>> uploadedFileUrls = new HashMap<>();

                    while (fileNames.hasNext()) {
                        String key = fileNames.next();
                        List<MultipartFile> files = multiRequest.getFiles(key);
                        List<String> urlList = new ArrayList<>();

                        for (MultipartFile file : files) {
                            String filename = file.getOriginalFilename();

                            if (filename == null || !filename.contains(".")) {
                                throw new RuntimeException("No file extension found");
                            }

                            try {
                                urlList.add(uploading(file));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                throw new RuntimeException("업로드 실패");
                            }
                        }

                        uploadedFileUrls.put(key, urlList);
                    }

                    while (i < noticeJsonDTO.getNoticeDetailDTO().size() )
                    {
                        List<String> beforeUrls = uploadedFileUrls.getOrDefault("before_" + i, new ArrayList<>());
                        List<String> afterUrls  = uploadedFileUrls.getOrDefault("after_" + i, new ArrayList<>());
                        NoticeDetailEntity noticeDetailEntity = ConvertToNoticeDetail(noticeJsonDTO.getNoticeDetailDTO().get(i),noticeEntity.getNoticeId(), beforeUrls, afterUrls );
                        NoticeDetailEntity noticeDetailEntity1 = noticeDetailService.InsertNoticeDetallMapper(noticeDetailEntity);
                        if(noticeDetailEntity1 != null)
                        {
                            noticeDetailEntities.add(noticeDetailEntity1);
                        }
                        else
                        {
                            noticeService.DeleteByNoticeId(notice1.getNoticeId());
                            throw new InsertFailedException("데이터 저장에 실패했습니다.");
                        }
                        i++;
                    }
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
            if(loginSuccess.isSessionExist(request))
            {
                NoticeEntity oldnotice = noticeService.findNoticeId(noticeJsonDTO.getNoticeDTO().getNoticeId());

                String Titleurl;
                if (titleimg != null && !titleimg.isEmpty()) {
                    // 새 파일 업로드
                    Titleurl = uploading(titleimg);
                    FileDelete(oldnotice.getTitleimg());
                }
                else
                {
                    // 기존 링크 그대로 사용
                    Titleurl = noticeJsonDTO.getNoticeDTO().getTitleimg();
                }

                NoticeEntity notice1 = noticeService.UpdateNotice(noticeJsonDTO.getNoticeDTO().getNoticeId(), ConvertToNotice(noticeJsonDTO.getNoticeDTO(),Titleurl));
                if(notice1 != null)
                {
                    int i = 0;
                    List<NoticeDetailEntity> noticeDetailEntities = new ArrayList<>();
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

                            try {
                                if (file != null && !file.isEmpty()) {
                                    urlList.add(uploading(file));
                                }
                                else
                                {
                                    String existUrl = null;
                                    if (type.equals("before")) {
                                        existUrl = noticeJsonDTO.getNoticeDetailDTO().get(index).getBeforeURL().get(fileIndex);
                                        System.out.println(existUrl);
                                    } else if (type.equals("after")) {
                                        existUrl = noticeJsonDTO.getNoticeDetailDTO().get(index).getAfterURL().get(fileIndex);
                                        System.out.println(existUrl);
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
                            NoticeDetailEntity noticeDetailEntity = noticeDetailService.noticeDetail(noticeJsonDTO.getNoticeDetailDTO().get(i).getNoticeDetailId());
                            FileDelete(noticeDetailEntity.getBeforeURL());
                            FileDelete(noticeDetailEntity.getAfterURL());
                            NoticeDetailEntity noticeDetailEntity1 = noticeDetailService.UpdateNoticeDetailMapper(noticeJsonDTO.getNoticeDetailDTO().get(i).getNoticeDetailId(), ConvertToNoticeDetail(noticeJsonDTO.getNoticeDetailDTO().get(i), beforeUrls,afterUrls));
                            if(noticeDetailEntity1 != null)
                            {
                                noticeDetailEntities.add(noticeDetailEntity1);
                                excludeIds.add(noticeDetailEntity1.getNoticeDetailId());
                            }
                            else
                            {
                                noticeService.UpdateNotice(noticeJsonDTO.getNoticeDTO().getNoticeId(), oldnotice);
                                throw new InsertFailedException("데이터 저장에 실패했습니다.");
                            }
                        }
                        else
                        {
                            NoticeDetailEntity noticeDetailEntity = ConvertToNoticeDetail(noticeJsonDTO.getNoticeDetailDTO().get(i),noticeJsonDTO.getNoticeDTO().getNoticeId(), beforeUrls, afterUrls);
                            NoticeDetailEntity noticeDetailEntity1 = noticeDetailService.InsertNoticeDetallMapper(noticeDetailEntity);
                            if(noticeDetailEntity1 != null)
                            {
                                noticeDetailEntities.add(noticeDetailEntity1);
                                excludeIds.add(noticeDetailEntity1.getNoticeDetailId());
                            }
                            else
                            {
                                noticeService.UpdateNotice(noticeJsonDTO.getNoticeDTO().getNoticeId(), oldnotice);
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
                        FileDelete(noticeDetail.getBeforeURL());
                        FileDelete(noticeDetail.getAfterURL());
                    }
                    NoticeEntity noticeEntity = noticeService.findNoticeId(noticeId);
                    if(noticeEntity != null)
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
            List<NoticeEntity> notice = new ArrayList<>(noticeService.findAll());
            if(notice != null)
            {

                for(NoticeEntity noticeEntity : notice)
                {
                    list.add(ConvertToNotice(noticeEntity));//자동으로 못가져오면 추가하거나 수정 예정
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
            List<NoticeEntity> notice = new ArrayList<>(noticeService.findAll2(page));
            if(notice != null)
            {

                for(NoticeEntity noticeEntity : notice)
                {
                    list.add(ConvertToNotice(noticeEntity));//자동으로 못가져오면 추가하거나 수정 예정
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
            List<NoticeEntity> notice = new ArrayList<>(noticeService.findSmallAll(CleanType.valueOf(type),page));
            if(notice != null)
            {
                for(NoticeEntity noticeEntity : notice)
                {
                    list.add(ConvertToNotice(noticeEntity));//자동으로 못가져오면 추가하거나 수정 예정
                }
                PageDTO pageDTO = PageDTO.builder()
                        .list(list)
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
            NoticeEntity notice = noticeService.findNoticeId(NoticeId);
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
    }

    private NoticeDetailEntity ConvertToNoticeDetail(NoticeDetailDTO noticeDetailDTO, String noticeId, List<String> beforeUrls, List<String> AfterUrls)
    {
        return NoticeDetailEntity.builder()
                .noticeId(noticeId)
                .noticeDetailId(noticeDetailDTO.getNoticeDetailId())
                .direction(noticeDetailDTO.getDirection())
                .beforeURL(beforeUrls)
                .afterURL(AfterUrls)
                .comment(noticeDetailDTO.getComment())
                .build();
    }

    private NoticeDetailEntity ConvertToNoticeDetail(NoticeDetailDTO noticeDetailDTO, List<String> beforeUrls, List<String> AfterUrls)
    {
        return NoticeDetailEntity.builder()
                .noticeId(noticeDetailDTO.getNoticeId())
                .noticeDetailId(noticeDetailDTO.getNoticeDetailId())
                .direction(noticeDetailDTO.getDirection())
                .beforeURL(beforeUrls)
                .afterURL(AfterUrls)
                .comment(noticeDetailDTO.getComment())
                .build();
    }

    private NoticeEntity ConvertToNotice(NoticeDTO noticeDTO, String Titleurl)
    {
        return NoticeEntity.builder()
                .noticeId(noticeDTO.getNoticeId())
                .type(noticeDTO.getType())
                .greeting(noticeDTO.getGreeting())
                .titleimg(Titleurl)
                .title(noticeDTO.getTitle())
                .build();
    }
    private NoticeEntity ConvertToNotice(NoticeDTO noticeDTO, String uniqueId, String url)
    {
        return NoticeEntity.builder()
                .noticeId(uniqueId)
                .type(noticeDTO.getType())
                .greeting(noticeDTO.getGreeting())
                .titleimg(url)
                .title(noticeDTO.getTitle())
                .build();
    }
    private NoticeDTO ConvertToNotice(NoticeEntity noticeEntity)
    {
        return NoticeDTO.builder()
                .noticeId(noticeEntity.getNoticeId())
                .type(noticeEntity.getType())
                .greeting(noticeEntity.getGreeting())
                .titleimg(noticeEntity.getTitleimg())
                .title(noticeEntity.getTitle())
                .build();
    }



    public String uploading( MultipartFile titleimg)
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
    public  void FileDelete(List<String> urllist)
    {
        try {
            for(String url : urllist)
            {
                s3ImageService.deleteImageFromS3(url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
