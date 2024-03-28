package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.request.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  RequestRepository extends JpaRepository<Request,Long> {
}

