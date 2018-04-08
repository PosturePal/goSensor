#include <jni.h>
#include <string>
#include <sstream>
#include <arpa/inet.h>

uint32_t deserialize_uint32(unsigned char *buffer) {
    uint32_t res = *((uint32_t *) buffer);
    return ntohl(res);
}
unsigned char* as_unsigned_char_array(JNIEnv *env, jbyteArray array) {
    int len = env->GetArrayLength (array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}


// TODO: use cpp14
template <typename T>
std::string to_string(T value)
{
    std::ostringstream os ;
    os << value ;
    return os.str() ;
}

// Get pointer field straight from `JavaClass`
jfieldID getPtrFieldId(JNIEnv *env, jobject obj) {
    static jfieldID ptrFieldId = 0;

    if (!ptrFieldId) {
        jclass c = env->GetObjectClass(obj);
        ptrFieldId = env->GetFieldID(c, "objPtr", "J");
        env->DeleteLocalRef(c);
    }

    return ptrFieldId;
}

extern "C"
{
JNIEXPORT jstring
JNICALL
Java_com_mathieu_sensorme_DevicesListAdapter_00024ViewHolder_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello = "sdjo";
    return env->NewStringUTF(hello.c_str());
}


JNIEXPORT jint
JNICALL
Java_com_mathieu_sensorme_DevicesListAdapter_00024ViewHolder_uint32FromBytes(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray bytes) {
    jbyte *m_bytes = env->GetByteArrayElements(bytes, 0);
    unsigned char *res = as_unsigned_char_array(env, bytes);
    uint32_t result = deserialize_uint32(res);
    return result;
}


JNIEXPORT jint
JNICALL
Java_com_mathieu_sensorme_DevicesListAdapter_00024ViewHolder_sint16FromBytes(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray bytes) {
    jbyte *m_bytes = env->GetByteArrayElements(bytes, 0);
    unsigned char *res = as_unsigned_char_array(env, bytes);
    // combine high and low bytes
    int16_t result = res[1] << 8 | res[0];
    return result;
}
//      0    2    3  4    5    6   7    8   9   10  11   12   13   14   15   16   17 18 19   20
//    [-40, -114, 0, 0,| -24, -1,| -6, -1,| -7, 3,| 122, 0|, -40, -18|, -13, -3,| 1, 0, -48, 34]
//    -- timestamp -- | - ax -  |- ay -  | - az -| - gx -| - gy -   | - gz -   | -- reserve --



}