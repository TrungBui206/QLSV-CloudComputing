
package hd.quanlysinhvien_hibernatedao.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    private static final String BUCKET = "test-documents";
    private static final String OBJECT_NAME = "HS001/document.pdf";

    @Mock
    private MinioClient minioClient;

    private MinioStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new MinioStorageService(
                minioClient,
                BUCKET
        );
    }

    // TEST 1: Upload khi bucket da ton tai
    @Test
    void upload_bucketExists_success() throws Exception {

        when(minioClient.bucketExists(
                any(BucketExistsArgs.class)
        )).thenReturn(true);

        byte[] data = "Hello MinIO"
                .getBytes(StandardCharsets.UTF_8);

        InputStream input = new ByteArrayInputStream(data);

        storageService.upload(
                OBJECT_NAME,
                input,
                data.length,
                "application/pdf"
        );

        verify(minioClient).bucketExists(
                any(BucketExistsArgs.class)
        );

        verify(minioClient, never()).makeBucket(
                any(MakeBucketArgs.class)
        );

        ArgumentCaptor<PutObjectArgs> captor =
                ArgumentCaptor.forClass(PutObjectArgs.class);

        verify(minioClient).putObject(captor.capture());

        assertEquals(
                BUCKET,
                captor.getValue().bucket()
        );

        assertEquals(
                OBJECT_NAME,
                captor.getValue().object()
        );
    }

    // TEST 2: Tu dong tao bucket neu chua co
    @Test
    void upload_bucketNotExists_createBucket() throws Exception {

        when(minioClient.bucketExists(
                any(BucketExistsArgs.class)
        )).thenReturn(false);

        byte[] data = "Test".getBytes(StandardCharsets.UTF_8);

        storageService.upload(
                OBJECT_NAME,
                new ByteArrayInputStream(data),
                data.length,
                "application/pdf"
        );

        ArgumentCaptor<MakeBucketArgs> bucketCaptor =
                ArgumentCaptor.forClass(MakeBucketArgs.class);

        verify(minioClient).makeBucket(
                bucketCaptor.capture()
        );

        assertEquals(
                BUCKET,
                bucketCaptor.getValue().bucket()
        );

        verify(minioClient).putObject(
                any(PutObjectArgs.class)
        );
    }

    // TEST 3: Loi khi kiem tra bucket
    @Test
    void upload_checkBucketError_throwException()
            throws Exception {

        when(minioClient.bucketExists(
                any(BucketExistsArgs.class)
        )).thenThrow(
                new RuntimeException("Connection failed")
        );

        byte[] data = "Test".getBytes(StandardCharsets.UTF_8);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.upload(
                        OBJECT_NAME,
                        new ByteArrayInputStream(data),
                        data.length,
                        "application/pdf"
                )
        );

        assertEquals(
                "Không thể kiểm tra hoặc tạo MinIO bucket",
                exception.getMessage()
        );

        assertNotNull(exception.getCause());

        verify(minioClient, never()).putObject(
                any(PutObjectArgs.class)
        );
    }

    // TEST 4: Loi khi tao bucket
    @Test
    void upload_createBucketError_throwException()
            throws Exception {

        when(minioClient.bucketExists(
                any(BucketExistsArgs.class)
        )).thenReturn(false);

        doThrow(
                new RuntimeException("Cannot create bucket")
        ).when(minioClient).makeBucket(
                any(MakeBucketArgs.class)
        );

        byte[] data = "Test".getBytes(StandardCharsets.UTF_8);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.upload(
                        OBJECT_NAME,
                        new ByteArrayInputStream(data),
                        data.length,
                        "application/pdf"
                )
        );

        assertEquals(
                "Không thể kiểm tra hoặc tạo MinIO bucket",
                exception.getMessage()
        );

        verify(minioClient, never()).putObject(
                any(PutObjectArgs.class)
        );
    }

    // TEST 5: Loi khi upload object
    @Test
    void upload_putObjectError_throwException()
            throws Exception {

        when(minioClient.bucketExists(
                any(BucketExistsArgs.class)
        )).thenReturn(true);

        when(minioClient.putObject(
                any(PutObjectArgs.class)
        )).thenThrow(
                new RuntimeException("Upload failed")
        );

        byte[] data = "Test".getBytes(StandardCharsets.UTF_8);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.upload(
                        OBJECT_NAME,
                        new ByteArrayInputStream(data),
                        data.length,
                        "application/pdf"
                )
        );

        assertEquals(
                "Không thể tải file lên MinIO",
                exception.getMessage()
        );

        assertNotNull(exception.getCause());
    }

    // TEST 6: Download thanh cong
    @Test
    void download_success() throws Exception {

        GetObjectResponse response =
                mock(GetObjectResponse.class);

        when(minioClient.getObject(
                any(GetObjectArgs.class)
        )).thenReturn(response);

        InputStream result =
                storageService.download(OBJECT_NAME);

        assertSame(response, result);

        ArgumentCaptor<GetObjectArgs> captor =
                ArgumentCaptor.forClass(GetObjectArgs.class);

        verify(minioClient).getObject(captor.capture());

        assertEquals(
                BUCKET,
                captor.getValue().bucket()
        );

        assertEquals(
                OBJECT_NAME,
                captor.getValue().object()
        );
    }

    // TEST 7: Download that bai
    @Test
    void download_error_throwException() throws Exception {

        when(minioClient.getObject(
                any(GetObjectArgs.class)
        )).thenThrow(
                new RuntimeException("Download failed")
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.download(OBJECT_NAME)
        );

        assertEquals(
                "Không thể tải file từ MinIO",
                exception.getMessage()
        );

        assertNotNull(exception.getCause());
    }

    // TEST 8: Xoa object thanh cong
    @Test
    void delete_success() throws Exception {

        storageService.delete(OBJECT_NAME);

        ArgumentCaptor<RemoveObjectArgs> captor =
                ArgumentCaptor.forClass(RemoveObjectArgs.class);

        verify(minioClient).removeObject(
                captor.capture()
        );

        assertEquals(
                BUCKET,
                captor.getValue().bucket()
        );

        assertEquals(
                OBJECT_NAME,
                captor.getValue().object()
        );
    }

    // TEST 9: Xoa object that bai
    @Test
    void delete_error_throwException() throws Exception {

        doThrow(
                new RuntimeException("Delete failed")
        ).when(minioClient).removeObject(
                any(RemoveObjectArgs.class)
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.delete(OBJECT_NAME)
        );

        assertEquals(
                "Không thể xóa file trên MinIO",
                exception.getMessage()
        );

        assertNotNull(exception.getCause());
    }
}
