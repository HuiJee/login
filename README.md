## 1. Login 페이지
![localhost_8080_login_generic (1)](https://github.com/user-attachments/assets/6247d4e9-e7dc-4d70-9437-ac0b4b8feb20)

### \# input 작성 시 파란 테두리
![image](https://github.com/user-attachments/assets/71c72f80-8e87-4919-8209-863b97c5ef38)

<span style="font-size:12px;">해당 칸이 채워진 경우에만 파란 테두리(클래스명)를 두고, 전부 적용된 경우에만 로그인 진행.</span>

![image](https://github.com/user-attachments/assets/eb6caa16-1390-4cfb-97a3-5056f9c570c3)

<span style="font-size:12px;">전부 작성이 이루어지지 않은 경우 '다시 시도하세요.' 문구가 3초간 보여짐.</span>

<br>

### \# 로그인 실패
![image](https://github.com/user-attachments/assets/1c853110-cbec-4400-b1a1-6ed3527329f7)

<span style="font-size:12px;">하단에 빨간 문구를 띄워 경고창을 대신함.</span>

<br>

### \# 아이디 기억하기
![image](https://github.com/user-attachments/assets/c315d910-3e21-4e4b-a813-c9b4c5d5bf45)

<span style="font-size:12px;">아이디 기억하기를 체크 후 로그인 시,
추후 재로그인 시도할 경우 해당 부분 체크와 함께 해당 ID가 이미 입력되어 있음.</span>

<br>

### \# 로그인 실행
- 프로필 화면으로 이동되며, 간단한 PK 및 로그인 아이디, 기억하기/자동로그인 선택은 LocalStorage에 저장.
- RefreshToken은 Redis에, AccessToken은 쿠키에 저장.
- RefreshToken은 일주일, AccessToken은 1일로 유효기간을 설정.

<br>

### \# 자동 로그인
- 자동 로그인 체크 후 로그인 시,
  로그인 페이지에 재접근할 경우 자동으로 <span style="text-decoration-line: underline;">4. 프로필</span> 화면으로 이동.
- 자동 로그인의 경우 RefreshToken의 유효기간은 30일로 설정.
- 로그아웃하거나 RefreshToken 마저 만료된 경우 로그인 창 접근 가능

https://github.com/user-attachments/assets/8aa73168-4d16-4625-8163-3c2ebdff88be

<br>
<br>

## 2. ID 찾기
![localhost_8080_login_find_id](https://github.com/user-attachments/assets/87a8e42d-5205-4bee-8c97-bc89d9d9ec32)

<br>

로그인과 동일하게 input 항목 입력 시, 파란 테두리 적용.

![image](https://github.com/user-attachments/assets/e2370ea8-12a9-42e9-a394-1c9b32e5e348)

<br>

정보 잘못 입력한 경우, 경고 모달 열림.

![image](https://github.com/user-attachments/assets/9281da3a-99ab-4fa7-b45e-474ef6a4be83)

<br>

입력한 정보가 일치한 경우, 아래와 같이 ID 전달.

![image](https://github.com/user-attachments/assets/4d8bbc14-a67b-4f4f-87b7-45ddf8e9095c)



<br>
<br>

## 3. PW 찾기

<br>
<br>

## 4. 프로필 (로그인 결과)
![localhost_8080_user_profile](https://github.com/user-attachments/assets/2ea3320f-5a61-4bbb-b9ec-3bd19a46279b)

### \# 권한
<span style="font-size:12px;">DB 상에는 A, B, C 등으로 저장이 되어있으며,
ENUM을 활용하여 출력 시 매칭되도록 설정.</span>

![image](https://github.com/user-attachments/assets/79606756-ec44-4839-b497-265f4d7ab525)

<br>
<br>

## 5. 로그아웃

프로필에서 로그아웃 선택 시, 확인 모달이 열림.
![image](https://github.com/user-attachments/assets/50baf039-986d-40d9-8b53-35747d08135d)

[확인] 선택한 경우,
쿠키에 남은 AccessToken과 Redis에 남은 RefreshToken, 
LocalStorage에 있는 [기억하기]를 제외한 모든 정보를 삭제 후 로그인페이지로 돌아옴.

<br>
<br>

## 6. 회원 가입

<br>
<br>

## 7. 소셜 로그인(Kakao)

