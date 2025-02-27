package com.shanjing.fingerprint.uitls;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * 加密类，用于判定指纹合法性
 */
@RequiresApi(Build.VERSION_CODES.M)
public class CipherHelper {
    // This can be key name you want. Should be unique for the app.
    static final String KEY_NAME = "com.hailong.fingerprint.CipherHelper";

    // We always use this keystore on Android.
    static final String KEYSTORE_NAME = "AndroidKeyStore";

    // Should be no need to change these values.
    static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    static final String BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
    static final String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
    static final String TRANSFORMATION = KEY_ALGORITHM + "/" +
            BLOCK_MODE + "/" +
            ENCRYPTION_PADDING;
    final KeyStore _keystore;

    public CipherHelper() throws Exception {
        _keystore = KeyStore.getInstance(KEYSTORE_NAME);
        _keystore.load(null);
    }

    /**
     * 获得Cipher
     * @return
     */
    public Cipher createCipher() {
        Cipher cipher = null;
        try {
            cipher = createCipher(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipher;
    }

    /**
     * 创建一个Cipher，用于 FingerprintManager.CryptoObject 的初始化
     * https://developer.android.google.cn/reference/javax/crypto/Cipher.html
     *
     * @param retry
     * @return
     * @throws
     */
    private Cipher createCipher(boolean retry) throws Exception {
        Key key = GetKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION); // Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE | Cipher.DECRYPT_MODE, key);
        } catch (KeyPermanentlyInvalidatedException e) {
            _keystore.deleteEntry(KEY_NAME);
            if (retry) {
                return createCipher(false);
            } else {
                throw new Exception("Could not create the cipher for fingerprint authentication.", e);
            }
        }
        return cipher;
    }

    private Key GetKey() throws Exception {
        Key secretKey;
        if (!_keystore.isKeyEntry(KEY_NAME)) {
            CreateKey();
        }

        secretKey = _keystore.getKey(KEY_NAME, null);
        return secretKey;
    }

    private void CreateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME);
        KeyGenParameterSpec keyGenSpec =
                new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(ENCRYPTION_PADDING)
                        .setUserAuthenticationRequired(true)
                        .build();
        keyGen.init(keyGenSpec);
        keyGen.generateKey();
    }
}