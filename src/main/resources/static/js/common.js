let USER_ID = localStorage.getItem('userId'); // User 테이블 pk
let USER_LOG_ID = localStorage.getItem('userLogId');  // User의 login id
let LOGIN_TYPE = localStorage.getItem('loginType');

let SAVED_USER_LOG_ID = localStorage.getItem('savedUserLogId');   // 아이디 기억하기 여부
let SAVED_AUTO_LOGIN = localStorage.getItem('savedAutoLogin');

const ONE = 1;
const TWO = 2;

const TRUE = "true";
const FALSE = "false";

const ID = "id";
const PW = "pw";

const GENERIC = "Generic";
const KAKAO = "Kakao";
const NAVER = "Naver";
const GOOGLE = "Google";

/** 페이지 로딩 시 변수 초기화 용 */
function updateGlobalVariables() {
    USER_ID = localStorage.getItem('userId');
    USER_LOG_ID = localStorage.getItem('userLogId');
    LOGIN_TYPE = localStorage.getItem('loginType');
}

/** 스토리지 삭제 (로그아웃 시, 리프레시 토큰 만료 시)*/
function localStorageDel() {
    const rememberValue = SAVED_USER_LOG_ID;

    window.localStorage.clear();

    if(rememberValue !== null) {
        localStorage.setItem('savedUserLogId', rememberValue);
    }
}

/** 스토리지 삭제 후 로그인 페이지로 이동시키기*/
function goLoginAgain() {
    localStorageDel();

    window.location.href="/login/generic";
}

/** 기능 실행 시 쿠키 확인하고 진행 (access 만료 시 refresh로 재발급)*/
async function tokenCheckFetch(url, options = {}) {

    if(USER_ID === null || USER_LOG_ID === null) {
        console.log('유저 정보가 없나??');
        // window.location.href="/login/generic";
        return;
    }

    let response = await fetch(url, {
        ...options,
    });

    console.log("Initial response status:", response.status);

    if (response.status === 401) {
        console.log("AccessToken expired, attempting to refresh...");

        const refreshTokenResponse = await fetch('/user/refresh-token', {
            headers: {
                'UserLogId': USER_LOG_ID,
            },
            method: 'POST',
        });

        console.log(refreshTokenResponse.status);

        if (refreshTokenResponse.ok) {
            console.log('refreshToken 존재!');
            // 리프레시 토큰으로 access 생성 후 다시 기능 시도
            response = await fetch(url, {
                ...options,
            });
        } else {
            console.error('Failed to refresh token:', refreshTokenResponse.status);
            // goLoginAgain();
            throw new Error('Unable to refresh token');
        }
    }
    else if (response.status === 403) {
        console.error("403 Forbidden: You don't have permission to access this resource.");
        // goLoginAgain();
        throw new Error('Forbidden: Access is denied');
    }
    return response;
}

