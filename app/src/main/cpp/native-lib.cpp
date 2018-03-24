#include <jni.h>
#include <string>
#include <sstream>

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
Java_co_vlad_cpp_cpp11gr_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello = "";
    return env->NewStringUTF(hello.c_str());
}
}