package hd.quanlysinhvien_hibernatedao.wrapper;

public record ApiResponse<T>(
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {

        return new ApiResponse<>(
                "SUCCESS",
                "Thành công",
                data
        );
    }

    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {

        return new ApiResponse<>(
                "SUCCESS",
                message,
                data
        );
    }
}