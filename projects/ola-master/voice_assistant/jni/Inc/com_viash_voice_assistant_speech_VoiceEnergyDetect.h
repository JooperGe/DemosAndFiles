#include <jni.h>

#ifndef _Included_com_viash_voice_assistant_speech_VoiceEnergyDetect
#define _Included_com_viash_voice_assistant_speech_VoiceEnergyDetect
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_viash_voice_1assistant_speech_VoiceEnergyDetect_startDetect
  (JNIEnv *, jclass);


JNIEXPORT jintArray JNICALL Java_com_viash_voice_1assistant_speech_VoiceEnergyDetect_addData
  (JNIEnv *, jclass, jbyteArray, jint);


JNIEXPORT void JNICALL Java_com_viash_voice_1assistant_speech_VoiceEnergyDetect_stopDetect
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
