//
// Created by Administrator on 2018/7/27.
//
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <stdlib.h>
#include <stdio.h>

#include "com_test_testgpio_GPIO.h"

#include "android/log.h"
static const char *TAG="Gpiotest";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

JNIEXPORT jint JNICALL Java_com_test_testgpio_GPIO_nativeWriteGpio
(JNIEnv *env, jclass thiz,jstring s, jint value)
{
  const char *path = (*env)->GetStringUTFChars(env, s, 0);
  char buf[100];
  int  n = -1;
  int gpio_fd = 0;
  gpio_fd  = open(path, O_WRONLY);
  	 if(gpio_fd <= 0)
  		{
  		 LOGI("gpio  init  error !!!");
  			return -1;
  		}
  sprintf(buf, "%d", value);
  LOGI("gpio  write  val:%s  \n",buf);
  	n = write (gpio_fd,&buf ,sizeof(buf));
  		if (n < 0) {
  		 LOGI("gpio  write  error !!!");
  		return -2;
  	    }
    close(gpio_fd);
    (*env)->ReleaseStringUTFChars(env, s, path);
    return 0;
}


JNIEXPORT jint JNICALL Java_com_test_testgpio_GPIO_nativeReadGpio
(JNIEnv *env, jclass thiz,  jstring s)
{
  const char *path = (*env)->GetStringUTFChars(env, s, 0);
  int  n = -1;
  int gpio_fd = 0;
  gpio_fd  = open(path, O_RDONLY);

  int  rval;
  char buf[100];
  	 if(gpio_fd <= 0)
  	 {
  		 LOGI("gpio  init  error !!!");
  		 return -1;
  	 }
  	 n = read(gpio_fd, &buf, 100);
  	 if (n < 0) {
  		 LOGI("gpio  read  error !!!");
  	     return -2;
  	 }

  	rval = atoi(buf);
  	LOGI("gpio  read  rval:%d  \n",rval);
  	(*env)->ReleaseStringUTFChars(env, s, path);
  	return rval;
}



