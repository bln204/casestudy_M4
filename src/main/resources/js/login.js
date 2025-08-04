document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.container');
    const btnSignIn = document.querySelector('.btnSign-in');
    const btnSignUp = document.querySelector('.btnSign-up');
    const loginButton = document.querySelector('.form.sign_in .btn.bkg');
    const registerButton = document.querySelector('.form.sign_up .btn.bkg');

    // Kiểm tra xem các phần tử có được tìm thấy không
    if (!container || !btnSignIn || !btnSignUp || !loginButton || !registerButton) {
        console.error('Một hoặc nhiều phần tử không được tìm thấy:', {
            container: !!container,
            btnSignIn: !!btnSignIn,
            btnSignUp: !!btnSignUp,
            loginButton: !!loginButton,
            registerButton: !!registerButton
        });
        return;
    }

    // Chuyển đến form đăng ký
    btnSignIn.addEventListener('click', () => {
        console.log('Chuyển sang form đăng ký');
        container.classList.add('active');
    });

    // Chuyển đến form đăng nhập
    btnSignUp.addEventListener('click', () => {
        console.log('Chuyển sang form đăng nhập');
        container.classList.remove('active');
    });

    // Xác thực client-side cho form đăng nhập
    loginButton.addEventListener('click', (event) => {
        const emailInput = document.querySelector('#email_signin').value.trim();
        const passwordInput = document.querySelector('#password_signin').value.trim();

        if (!emailInput || !passwordInput) {
            event.preventDefault();
            alert('Vui lòng điền đầy đủ email và mật khẩu!');
        }
    });

    // Xác thực client-side cho form đăng ký
    registerButton.addEventListener('click', (event) => {
        const nameInput = document.querySelector('#name').value.trim();
        const emailInput = document.querySelector('#email_signup').value.trim();
        const passwordInput = document.querySelector('#password_signup').value.trim();

        if (!nameInput || !emailInput || !passwordInput) {
            event.preventDefault();
            alert('Vui lòng điền đầy đủ thông tin!');
            return;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailInput)) {
            event.preventDefault();
            alert('Vui lòng nhập địa chỉ email hợp lệ!');
            return;
        }

        if (passwordInput.length < 6) {
            event.preventDefault();
            alert('Mật khẩu phải có ít nhất 6 ký tự!');
        }
    });
});