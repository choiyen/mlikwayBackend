package project.MilkyWay.S3ClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MultiS3ImageService
{

    @Autowired
    S3ImageService s3ImageService;

    @Async("taskExecutor")  // ThreadPoolConfig에서 정의한 이름
    public CompletableFuture<List<String>> S3Import(List<MultipartFile> flies)
    {
        List<String> S3ImageURL = new ArrayList<>();
        try
        {
            for(MultipartFile file : flies)
            {
                String filename = file.getOriginalFilename();
                if (filename == null || !filename.contains(".")) {
                    throw new RuntimeException("No file extension found");
                }

                String url = s3ImageService.upload(file);
               S3ImageURL.add(url);
            }
            Thread.sleep(2000); // 예제용 딜레이
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture(S3ImageURL);
    }
}
