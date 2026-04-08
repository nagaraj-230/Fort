/****************************************************************************
 * File:   toolChecker.h
 * Author: Matthew Rollings
 * Date:   19/06/2015
 *
 * Description : Root checking JNI NDK code
 *
 ****************************************************************************/

extern "C" {

#include <jni.h>

void Java_com_payoda_rootchecker_SourceIDENative_setLogDebugMessages(JNIEnv *env, jobject thiz,
                                                                   jboolean debug);

int Java_com_payoda_rootchecker_SourceIDENative_checkForRoot(JNIEnv *env, jobject thiz,
                                                           jobjectArray pathsArray);

}
