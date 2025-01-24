package com.hjpj.login.auth;

import com.hjpj.login.common.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService{

    /** 헤더에서 logId 얻어 토큰 작업하기 */
    public void getAccessFromRefreshByUserLogId(HttpServletRequest request, HttpServletResponse response) {
        String userLogId = request.getHeader(CommonUtils.USER_LOG_ID_NAME);

        TokenUtils.getAccessFromRefresh(userLogId, response);
    }

}
