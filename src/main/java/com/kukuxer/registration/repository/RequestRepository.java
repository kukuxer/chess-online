package com.kukuxer.registration.repository;

import com.kukuxer.registration.domain.requests.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  RequestRepository extends JpaRepository<Request,Long> {
}

