package jp.clip.typinggame.exception.handler;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jp.clip.typinggame.exception.CustomFieldError;
import jp.clip.typinggame.exception.ErrorResponse;

/**
 * API共通の例外レスポンスを生成するハンドラーです。
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Bean Validationのエラーを共通エラーレスポンスへ変換します。
     *
     * @param ex validation例外
     * @param headers レスポンスヘッダー
     * @param status HTTPステータス
     * @param request Webリクエスト
     * @return 共通エラーレスポンス
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<CustomFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toCustomFieldError)
                .toList();

        ErrorResponse response = new ErrorResponse(fieldErrors);
        return handleExceptionInternal(ex, response, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * JSONの形式不正やenum変換エラーを共通エラーレスポンスへ変換します。
     *
     * @param ex JSON読み取り例外
     * @param headers レスポンスヘッダー
     * @param status HTTPステータス
     * @param request Webリクエスト
     * @return 共通エラーレスポンス
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponse response = ErrorResponse.of(
                "INVALID_REQUEST",
                "",
                "リクエストの形式が正しくありません。");
        return handleExceptionInternal(ex, response, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * ControllerやServiceから送出されたHTTPステータス付き例外を共通エラーレスポンスへ変換します。
     *
     * @param ex HTTPステータス付き例外
     * @return 共通エラーレスポンス
     */
    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        String errorCode = status == null ? "API_ERROR" : status.name();
        String message = ex.getReason() == null ? "処理に失敗しました。" : ex.getReason();

        return ResponseEntity.status(ex.getStatusCode())
                .body(ErrorResponse.of(errorCode, "", message));
    }

    /**
     * RequestParamなどのvalidationエラーを共通エラーレスポンスへ変換します。
     *
     * @param ex validation例外
     * @return 共通エラーレスポンス
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<CustomFieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::toCustomFieldError)
                .toList();

        return ResponseEntity.badRequest().body(new ErrorResponse(fieldErrors));
    }

    /**
     * SpringのFieldErrorをAPI用のエラー情報へ変換します。
     *
     * @param fieldError Springのフィールドエラー
     * @return API用のエラー情報
     */
    private CustomFieldError toCustomFieldError(FieldError fieldError) {
        String errorCode = fieldError.getCode() == null ? "VALIDATION_ERROR" : fieldError.getCode();
        return new CustomFieldError(errorCode, fieldError.getField(), fieldError.getDefaultMessage());
    }

    /**
     * ConstraintViolationをAPI用のエラー情報へ変換します。
     *
     * @param violation validationエラー
     * @return API用のエラー情報
     */
    private CustomFieldError toCustomFieldError(ConstraintViolation<?> violation) {
        return new CustomFieldError(
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                extractFieldName(violation),
                violation.getMessage());
    }

    /**
     * validationエラーのプロパティパスからフィールド名を取得します。
     *
     * @param violation validationエラー
     * @return フィールド名
     */
    private String extractFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastSeparatorIndex = propertyPath.lastIndexOf('.');
        if (lastSeparatorIndex < 0) {
            return propertyPath;
        }
        return propertyPath.substring(lastSeparatorIndex + 1);
    }
}
