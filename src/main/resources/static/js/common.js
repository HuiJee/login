const USER_ID = localStorage.getItem('userId'); // User 테이블 pk
const USER_LOG_ID = localStorage.getItem('userLogId');  // User의 login id

const SAVED_USER_LOG_ID = localStorage.getItem('savedUserLogId');   // 아이디 기억하기 여부
const SAVED_AUTO_LOGIN = localStorage.getItem('autoLogin');

const ONE = 1;
const TWO = 2;

const TRUE = "true";
const FALSE = "false";

const ID = "id";
const PW = "pw";


/** 로그인 여부에 따른 사이드 바 메뉴 수정*/
const loginBeforeMenu = document.querySelectorAll('.login-before');
const loginAfterMenu = document.querySelectorAll('.login-after');

loginBeforeMenu.forEach((menu) => {
    USER_ID !== null ?
    menu.classList.add('d-none') : menu.classList.remove('d-none');
});

loginAfterMenu.forEach((menu) => {
    USER_ID !== null ?
    menu.classList.remove('d-none') : menu.classList.add('d-none');
});


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

    console.log('userId : ', USER_ID);
    console.log('userLogId : ', USER_LOG_ID);

    if(USER_ID === null || USER_LOG_ID === null) {
        window.location.href="/login/generic";
        return;
    }

    console.log('url : ', url);
    console.log('options : ', options);

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
            goLoginAgain();
            throw new Error('Unable to refresh token');
        }
    }
    else if (response.status === 403) {
        console.error("403 Forbidden: You don't have permission to access this resource.");
        goLoginAgain();
        throw new Error('Forbidden: Access is denied');
    }
    return response;
}

document.querySelector('.close-btn').addEventListener('click', (e) => {
    const parentModal = e.target.closest('.modal');
    parentModal.style.display = 'none';
});

