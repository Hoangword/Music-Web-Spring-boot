package com.HuyHoang.service;

import com.HuyHoang.DTO.MailBody;
import com.HuyHoang.DTO.request.ChangePasswordRequest;
import com.HuyHoang.DTO.request.VerifyEmailRequest;
import com.HuyHoang.Entity.ForgotPassword;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.repository.ForgotPasswordRepository;
import com.HuyHoang.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    @NonFinal
    @Value("${spring.mail.username}")
    protected String EmailFrom;

    UserRepository userRepository;

    JavaMailSender javaMailSender;

    ForgotPasswordRepository forgotPasswordRepository;

    PasswordEncoder passwordEncoder;




    public void sendSimpleMessage(MailBody mailBody){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailBody.to());
        message.setFrom(EmailFrom);
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());

        javaMailSender.send(message);
    }

    public String verifyEmail(String email){

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        int otp = otpGenerator();
        forgotPasswordRepository.findByUser(user).ifPresent(forgotPassword -> forgotPasswordRepository.deleteById(forgotPassword.getFpid()));

        ForgotPassword forgotPassword = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(Date.from(Instant.now().plus(10, ChronoUnit.MINUTES)))
                .user(user)
                .build();
        forgotPasswordRepository.save(forgotPassword);
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("Ma Otp tao lai mat khau cua ban la: "+ otp + "\n Ma nay co hieu luc trong 10 phut")
                .subject("OTP quen mat khau")
                .build();


        sendSimpleMessage(mailBody);
        return "OTP da duoc gui den email cua ban.";
    }
    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000,999_999);
    }

    public String changePasswordHandler(ChangePasswordRequest request, String email){
        if(!Objects.equals(request.password(),request.repeatPassword())){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        ForgotPassword forgotPassword = forgotPasswordRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_OR_USER_INVALID));
        if (forgotPassword.getExpirationTime().before(Date.from(Instant.now()))) {
            user.setForgotPassword(null);
            forgotPasswordRepository.deleteById(forgotPassword.getFpid());
            throw new AppException(ErrorCode.OTP_ALREADY_EXPIRED);
        }
        user.setForgotPassword(null);
        forgotPasswordRepository.deleteById(forgotPassword.getFpid());
        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPassword(encodedPassword);
        userRepository.save(user);
        return "Password has been changed";
    }

    public String verifyOtpAndUser(Integer Otp , String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.EMAIL_INVALID));
        ForgotPassword forgotPassword = forgotPasswordRepository.findByOtpAndUser(Otp,user).orElseThrow(() -> new AppException(ErrorCode.OTP_OR_USER_INVALID));

        if(forgotPassword.getExpirationTime().before(Date.from(Instant.now()))){
            forgotPasswordRepository.deleteById(forgotPassword.getFpid());
            throw new AppException(ErrorCode.OTP_ALREADY_EXPIRED);
        }


        return "Otp hop le";
    }
}
