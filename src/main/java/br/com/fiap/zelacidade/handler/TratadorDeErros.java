package br.com.fiap.zelacidade.handler;

import br.com.fiap.zelacidade.dto.ErroDTO;
import br.com.fiap.zelacidade.exception.AcessoDadosException;
import br.com.fiap.zelacidade.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelacidade.exception.RegraNegocioException;
import br.com.fiap.zelacidade.exception.RequisicaoInvalidaException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Tratador global: transforma qualquer erro no JSON padrão do contrato
 * { "status": ..., "mensagem": "..." }. Nunca devolve stack trace, SQL ou HTML.
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

    // ---------- nossas exceções ----------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> naoEncontrado(RecursoNaoEncontradoException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroDTO> regraNegocio(RegraNegocioException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroDTO> requisicaoInvalida(RequisicaoInvalidaException e) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // ---------- erros da requisição ----------

    /** Bean Validation (@Valid): devolve a mensagem do primeiro campo com problema. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> validacao(MethodArgumentNotValidException e) {
        FieldError campo = e.getBindingResult().getFieldError();
        String mensagem = campo != null ? campo.getDefaultMessage() : "Dados inválidos.";
        return erro(HttpStatus.BAD_REQUEST, mensagem);
    }

    /** JSON malformado, enum que não existe ou tipo errado (ex.: "gravidade": "alta"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> jsonInvalido(HttpMessageNotReadableException e) {
        Throwable causa = e.getCause();
        if (causa instanceof InvalidFormatException formato && !formato.getPath().isEmpty()) {
            String campo = formato.getPath().get(formato.getPath().size() - 1).getFieldName();
            Class<?> tipo = formato.getTargetType();
            if (tipo != null && tipo.isEnum()) {
                String validos = Arrays.stream(tipo.getEnumConstants()).map(Object::toString)
                        .collect(Collectors.joining(", "));
                return erro(HttpStatus.BAD_REQUEST, "Valor inválido para \"" + campo + "\". Use um destes: " + validos + ".");
            }
            return erro(HttpStatus.BAD_REQUEST, "O campo \"" + campo + "\" está com um valor do tipo errado.");
        }
        if (causa instanceof MismatchedInputException entrada && !entrada.getPath().isEmpty()) {
            String campo = entrada.getPath().get(entrada.getPath().size() - 1).getFieldName();
            return erro(HttpStatus.BAD_REQUEST, "O campo \"" + campo + "\" está com um valor do tipo errado.");
        }
        return erro(HttpStatus.BAD_REQUEST, "O corpo da requisição não é um JSON válido. Confira vírgulas, aspas e chaves.");
    }

    /** Ex.: GET /ocorrencias/abc (o id precisa ser número). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDTO> tipoNaUrl(MethodArgumentTypeMismatchException e) {
        return erro(HttpStatus.BAD_REQUEST, "O valor \"" + e.getValue() + "\" não é válido para \"" + e.getName()
                + "\". Use um número inteiro.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroDTO> metodoNaoPermitido(HttpRequestMethodNotSupportedException e) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED, "O método " + e.getMethod() + " não é permitido nesta rota.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroDTO> tipoDeConteudo(HttpMediaTypeNotSupportedException e) {
        return erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie o corpo em JSON (Content-Type: application/json).");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErroDTO> rotaInexistente(Exception e) {
        return erro(HttpStatus.NOT_FOUND, "Rota não encontrada. Confira o endereço e o método.");
    }

    // ---------- erros do servidor ----------

    @ExceptionHandler(AcessoDadosException.class)
    public ResponseEntity<ErroDTO> banco(AcessoDadosException e) {
        LOG.error("Falha no banco de dados", e);   // o detalhe fica só no log do servidor
        return erro(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não conseguimos acessar o banco de dados agora. Tente de novo em instantes.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroDTO> inesperado(Exception e) {
        LOG.error("Erro inesperado", e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente de novo mais tarde.");
    }

    private ResponseEntity<ErroDTO> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroDTO(status.value(), mensagem));
    }
}
