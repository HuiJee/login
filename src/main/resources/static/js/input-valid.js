
// 필수 값 입력 시 파란테두리
function inputBorderPrimary(inputs) {
    inputs.forEach((input) => {
        input.addEventListener('input', () => {
            if (input.value.trim() !== "") {
                input.classList.add('border-primary');
                input.classList.remove('border-danger');
            } else {
                input.classList.remove('border-primary');
            }
        });
    });
}

// 필수 값 입력 없이 제출 클릭할 경우 빨간 테두리
function inputBorderDanger(inputs) {
    inputs.forEach((input) => {
        input.classList.add('border-danger');
    })
}