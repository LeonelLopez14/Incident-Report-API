package com.castagno.dev.incident_reports_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO genérico para respuestas paginadas.
 */
@Getter
@Builder
@Schema(description = "Respuesta paginada genérica")
public class PageResponse<T> {

    @Schema(description = "Lista de elementos de la página actual")
    private final List<T> content;

    @Schema(description = "Número de página actual (base 0)", example = "0")
    private final int page;

    @Schema(description = "Cantidad de elementos por página", example = "10")
    private final int size;

    @Schema(description = "Total de elementos en todas las páginas", example = "47")
    private final long totalElements;

    @Schema(description = "Total de páginas", example = "5")
    private final int totalPages;

    @Schema(description = "Indica si es la última página", example = "false")
    private final boolean last;

    @Schema(description = "Indica si es la primera página", example = "true")
    private final boolean first;

    /**
     * Factory method: construye un PageResponse a partir de un Page de Spring Data.
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}