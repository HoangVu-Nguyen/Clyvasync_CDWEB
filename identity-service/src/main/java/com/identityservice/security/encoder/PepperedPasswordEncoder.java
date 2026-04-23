package com.identityservice.security.encoder;



import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PepperedPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate; // Đây sẽ là Argon2PasswordEncoder
    private final String pepper;

    public PepperedPasswordEncoder(PasswordEncoder delegate, String pepper) {
        this.delegate = delegate;
        this.pepper = pepper;
    }

    /**
     * Biến đổi mật khẩu thô thành chuỗi mã hóa kép
     */
    private String hashWithPepper(CharSequence rawPassword) {
        String secretCombo = rawPassword.toString() + pepper;


        return DigestUtils.sha512Hex(secretCombo);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(hashWithPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(hashWithPepper(rawPassword), encodedPassword);
    }
}