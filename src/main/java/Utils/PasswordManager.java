package Utils;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides AES encryption/decryption for password management.
 * NOTE:
 * - Uses ECB mode (not recommended for production with repetitive data patterns, we can consider CBC/GCM with random IVs later)
 * - Contains static secret key (should use secure key management in production)
 * - Uses SHA-1 for key derivation (consider stronger hashes for critical systems, this handles current use case, we can look into PBKDF2 later)
 */
public class PasswordManager {
	// Encryption configuration constants
	private static final String SECRET_KEY = "DemoNCUA"; // Should be stored securely in production
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding"; // ECB lacks semantic security

	// Static initialization for key validation
	static {
		validateKeyLength();
	}

	/**
	 * Validates and prepares secret key during class initialization.
	 * Converts raw key to proper AES-128 key using SHA-1 hashing and truncation.
	 * @throws RuntimeException if key processing fails
	 */
	private static void validateKeyLength() {
		try {
			byte[] key = SECRET_KEY.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // Truncate to 128-bit (16 byte) key
			new SecretKeySpec(key, ALGORITHM); // Validate key specification
		} catch (Exception e) {
			throw new RuntimeException("Key validation failed", e);
		}
	}

	/**
	 * Encrypts plaintext password using AES-128
	 * @param password Plain text password to encrypt
	 * @return Base64-encoded encrypted string
	 * @throws RuntimeException if encryption fails
	 */
	public static String encrypt(String password) {
		try {
			// Key derivation process
			byte[] key = SECRET_KEY.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // Ensure 128-bit key length

			SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			// Perform encryption and Base64 encoding
			byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			throw new RuntimeException("Encryption failed", e);
		}
	}

	/**
	 * Decrypts Base64-encoded ciphertext
	 * @param encryptedPassword Encrypted string from encrypt() method
	 * @return Original plaintext password
	 * @throws RuntimeException if decryption fails
	 */
	public static String decrypt(String encryptedPassword) {
		try {
			// Key derivation (must match encrypt process)
			byte[] key = SECRET_KEY.getBytes("UTF-8");
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);

			SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			// Base64 decode and decrypt
			byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
			return new String(cipher.doFinal(decodedBytes), "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Decryption failed", e);
		}
	}

	/**
	 * Dry run method to create encrypted password
	 * One time setup for encryption
	 * Prints out an encrypted password using encrypt() method
	 */
	//  public static void main(String[] args) {
	//  // Dry run test
	//  String originalPassword = "Test123@#";
	//  
	//  String encrypted = encrypt(originalPassword);
	//  System.out.println("Encrypted: " + encrypted);
	//  
	//  String decrypted = decrypt(encrypted);
	//  System.out.println("Decrypted: " + decrypted);
	//}

}