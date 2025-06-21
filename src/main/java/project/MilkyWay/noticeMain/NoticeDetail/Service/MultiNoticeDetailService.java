package project.MilkyWay.noticeMain.NoticeDetail.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.MilkyWay.ComonType.Expection.InsertFailedException;
import project.MilkyWay.noticeMain.Notice.Service.NoticeService;
import project.MilkyWay.noticeMain.NoticeDetail.DTO.NoticeDetailDTO;
import project.MilkyWay.noticeMain.NoticeDetail.Entity.NoticeDetailEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class MultiNoticeDetailService
{
    @Autowired
    NoticeDetailService noticeDetailService;

    @Autowired
    NoticeService noticeService;

    private final Executor taskExecutor;

    public MultiNoticeDetailService(@Qualifier("taskExecutor") Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Async("taskExecutor")
    public CompletableFuture<List<NoticeDetailDTO>> S3Import(String noticeId, List<NoticeDetailDTO> noticeDetailDTOS, Map<String, List<String>> uploadedFileUrls) {
        List<CompletableFuture<NoticeDetailDTO>> futures = new ArrayList<>();

        for (int i = 0; i < noticeDetailDTOS.size(); i++) {
            final int index = i;  // 람다 안에서 사용하려면 final 혹은 effectively final 필요
            CompletableFuture<NoticeDetailDTO> future = CompletableFuture.supplyAsync(() -> {
                List<String> beforeUrls = uploadedFileUrls.getOrDefault("before_" + index, new ArrayList<>());
                List<String> afterUrls  = uploadedFileUrls.getOrDefault("after_" + index, new ArrayList<>());

                NoticeDetailDTO savedEntity = noticeDetailService.InsertNoticeDetallMapper(noticeDetailDTOS.get(index), noticeId, beforeUrls, afterUrls);
                System.out.println(savedEntity);

                if (savedEntity == null) {
                    // DB 저장 실패 시 예외 발생
                    throw new InsertFailedException("데이터 저장에 실패했습니다.");
                }

                return savedEntity;
            }, taskExecutor);

            futures.add(future);
            System.out.println(futures);
        }

        // 모든 작업이 끝날 때까지 기다리고 결과 리스트로 변환
        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join) // join()은 unchecked exception 던짐
                        .collect(Collectors.toList())
        );
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

}
