/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* Apache 2.0 License
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package io.py2nium.server.gui.utils;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;


public class KeyStoringHelper {
    public static PrivateKey getPrivateKey(String alias, char[] password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            return (PrivateKey)keyStore.getKey(alias, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Certificate getCertificate(String alias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            return keyStore.getCertificate(alias);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean generateNewInfo(String alias, char[] password) {
        int keySize = 2048;

        try {
            KeyPairGenerator keyPairGenerator = null;
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize, SecureRandom.getInstance("SHA1PRNG"));
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X500Name issuer = new X500Name("CN=Py2nium");
            X500Name subject = new X500Name("CN=Py2nium");
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
            Date notBefore = new Date(System.currentTimeMillis());
            Date notAfter = new Date(System.currentTimeMillis() + 2L * 365 *  86400000L);
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(keyPair.getPublic().getEncoded()));
            X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, publicKeyInfo);

            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                                    .setProvider("AndroidOpenSSL")
                                    .build(keyPair.getPrivate());

            X509CertificateHolder certificateHolder = certificateBuilder.build(signer);
            X509Certificate certificate = new JcaX509CertificateConverter()
                                            .setProvider("AndroidOpenSSL")
                                            .getCertificate(certificateHolder);

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            keyStore.setKeyEntry(alias, keyPair.getPrivate(), password, new Certificate[]{certificate});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
