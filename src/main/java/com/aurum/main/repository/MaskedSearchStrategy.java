package com.aurum.main.repository;

import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.model.Employee;

public interface MaskedSearchStrategy<T, Q> {
    PageResponse<T> searchMasked(Q query);
}