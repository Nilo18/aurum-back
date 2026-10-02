package com.aurum.main.repository;

import com.aurum.main.dto.responses.PageResponse;

public interface SearchStrategy<T, Q> {
    PageResponse<T> search(Q query);
}
