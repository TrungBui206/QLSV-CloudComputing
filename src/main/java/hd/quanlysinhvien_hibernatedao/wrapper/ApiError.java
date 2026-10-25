package hd.quanlysinhvien_hibernatedao.wrapper;

import java.time.LocalDateTime;

public record ApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String code
) {

    public static ApiError of(
            int status,
            String error,
            String message,
            String path,
            String code
    ) {

        return new ApiError(
                LocalDateTime.now().toString(),
                status,
                error,
                message,
                path,
                code
        );
    }
}