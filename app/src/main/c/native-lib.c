#include <jni.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

JNIEXPORT jstring JNICALL Java_com_payoda_smartlock_service_AESEncryption_getEncryptionCypher
        (JNIEnv *env, jobject obj) {
    return (*env)->NewStringUTF(env, "AES/CBC/PKCS7Padding");
}

JNIEXPORT jstring JNICALL Java_com_payoda_smartlock_service_AESEncryption_getEncryptionKey
        (JNIEnv *env, jobject obj) {
    return (*env)->NewStringUTF(env, "smartlockkey");
}

JNIEXPORT jstring JNICALL Java_com_payoda_smartlock_service_AESEncryption_getEncryptionVector
        (JNIEnv *env, jobject obj) {
    return (*env)->NewStringUTF(env, "8119745113154120");
}

JNIEXPORT jstring JNICALL Java_com_payoda_smartlock_constants_ServiceUrl_getLockURL
        (JNIEnv *env, jobject obj) {
    return (*env)->NewStringUTF(env, "http://192.168.4.1");
}

JNIEXPORT jstring JNICALL Java_com_payoda_smartlock_locks_WifiMqttConfigurationFragment_getMqttIp
        (JNIEnv *env, jobject obj, jstring buildVariant) {
    const char *variant = (*env)->GetStringUTFChars(env, buildVariant, NULL);
    char localVariantD[2] = "D";
    char localVariantS[2] = "S";

    if (strcmp(variant, localVariantD) == 0) {
        return (*env)->NewStringUTF(env, "smartlock.payoda.com");
    } else if (strcmp(variant, localVariantS) == 0) {
        return (*env)->NewStringUTF(env, "stage.smartlocks.co.in");
    } else { //P
        return (*env)->NewStringUTF(env, "");
    }
}

JNIEXPORT jstring JNICALL Java_com_payoda_smartlock_locks_WifiMqttConfigurationFragment_getMqttPort
        (JNIEnv *env, jobject obj, jstring buildVariant) {
    const char *variant = (*env)->GetStringUTFChars(env, buildVariant, NULL);
    char localVariantD[2] = "D";
    char localVariantS[2] = "S";

    if (strcmp(variant, localVariantD) == 0) {
        return (*env)->NewStringUTF(env, "1883");
    } else if (strcmp(variant, localVariantS) == 0) {
        return (*env)->NewStringUTF(env, "1883");
    } else { //P
        return (*env)->NewStringUTF(env, "");
    }
}

JNIEXPORT jstring JNICALL Java_com_payoda_smartlock_plugins_network_ServiceManager_getPublicKey
        (JNIEnv *env, jobject obj, jstring buildVariant, jstring bundleIdentifier) {
    const char *variant = (*env)->GetStringUTFChars(env, buildVariant, NULL);
    const char *bundleId = (*env)->GetStringUTFChars(env, bundleIdentifier, NULL);
    char localVariantD[2] = "D";
    char localVariantS[2] = "S";
    char ax100[32] = "com.astrixengineering.ax100";

    if (strcmp(variant, localVariantD) == 0) {
        return (*env)->NewStringUTF(env,
                                    "30820122300d06092a864886f70d01010105000382010f003082010a0282010100b614015a1cf7660e1f8e53870e6cf166c48edcce31db4334fb04bc6f0b8ef8836b8249e485f62e0825051a27f9a0512b4a35b07ef49caa7d74e0cbda60ec26144d1b132669efb8206dea605404f83cc1905af82c595ce00a86671b756f7239b0e5561463ac73c7e449c405dc7bebd1ca9b9c39653a18544791000e03619364ff350dec8c5c91e32702e51a606984aaa8e4f675016ad4b004c79ef28700f5518575768bc9b1c83f70b951742f92859a9bf59c6612af899fb434f8ee50a575bc131377ff637541d15249e5b3cc281b933e9d342568d76c123b481b76b10333a6274fc9f0b20f6141033d1ed9cdda8ac7882d4ccbe2616be015cc739d7fb5418e5b0203010001");
    } else if (strcmp(variant, localVariantS) == 0) {
        return (*env)->NewStringUTF(env,
                                    "30820122300d06092a864886f70d01010105000382010f003082010a0282010100b75c2f66636b4118f0be496bbfc6878dc9bfe0f6ac487156cd85f3b899c4238095c60ee39018b79cd619436904febcae04c26546ac994782672034e56c965875f80b2966a0d13ab2cfb0858bc0d421d713fa9a138d669b9159591490100b26ec0ff41b16f81ce199a74b645b72b7ef2a398bcb76181be04db8dbe816efe35f709a67276dd06771652eadcb15695d001f3399ea9d2a912c852af1f2d094824705273f051190545f64fa5d7e657bc2292aa73a814b5547ac3cec7db732539c4136faf6c18f785f371197dc958494a1d59ba9e834cbde71bea30c31c00d201d71deb86c8f0d32b28e4b227de355832db43ea378e83f9de06e4e5dabf0731547c19b0203010001");
    } else { //P
        if (strcmp(bundleId, ax100) == 0) {
            return (*env)->NewStringUTF(env,
                                        "30820122300d06092a864886f70d01010105000382010f003082010a0282010100b4e434ae971de412ef9d94abd266598b19967e72fd47e4beb66cda4d9b02589b152c017e7a832d3bbc43a097d2a5c1ef61fd0ca52b504651887942f7cb474375e1a21b9b3355c111eda01934876301a2b78214dda003deda14857c67e2796a714b18c2cd88bdf4a9a61eccd4e29dfa65375db290b0aaab76f4e0c4859f51fa4d180c1ce611133f7f20fcaf5bf8ecabcd4da09309abf75e05248bef60c94b4a7b8bd4ae30f4e35dabd83e2f3a03aa7cd84b957b244aadbd546ab81e7bb2549e5af1bd7bb4f9ef53682161ebdeecee2a4eac3cdf1731638cb6faf5bd7906a80c09267c05fe00f68b4be9c0952cdbc8d7701dca79c28083993ee18b4bb9fce51e090203010001");
        } else { // common for Secnor and Touchplus
            return (*env)->NewStringUTF(env,
                                        "30820122300d06092a864886f70d01010105000382010f003082010a0282010100b4e434ae971de412ef9d94abd266598b19967e72fd47e4beb66cda4d9b02589b152c017e7a832d3bbc43a097d2a5c1ef61fd0ca52b504651887942f7cb474375e1a21b9b3355c111eda01934876301a2b78214dda003deda14857c67e2796a714b18c2cd88bdf4a9a61eccd4e29dfa65375db290b0aaab76f4e0c4859f51fa4d180c1ce611133f7f20fcaf5bf8ecabcd4da09309abf75e05248bef60c94b4a7b8bd4ae30f4e35dabd83e2f3a03aa7cd84b957b244aadbd546ab81e7bb2549e5af1bd7bb4f9ef53682161ebdeecee2a4eac3cdf1731638cb6faf5bd7906a80c09267c05fe00f68b4be9c0952cdbc8d7701dca79c28083993ee18b4bb9fce51e090203010001");
        }
    }
}