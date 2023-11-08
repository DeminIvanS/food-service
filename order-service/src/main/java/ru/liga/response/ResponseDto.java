package ru.liga.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ResponseDto<E> {

    private List<E> orders;
    private int pageIndex;
    private int pageCount;
}
