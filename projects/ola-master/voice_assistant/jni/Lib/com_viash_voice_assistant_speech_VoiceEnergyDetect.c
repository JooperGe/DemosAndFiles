#include <jni.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>
#include <math.h>
//Leo
#include <android/log.h>
#define LOG_TAG "cqEmbed"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#ifndef _Included_com_viash_voice_assistant_speech_VoiceEnergyDetect
#define _Included_com_viash_voice_assistant_speech_VoiceEnergyDetect
#ifdef __cplusplus
extern "C" {
#endif


#define SWAP(a,b) tempr=(a);(a)=(b);(b)=tempr

int lastEndPoint = 0;
JNIEXPORT void JNICALL Java_com_viash_voice_1assistant_speech_VoiceEnergyDetect_startDetect(JNIEnv *jniEnv, jclass jc)
{
	lastEndPoint = 0;
}

void fft(double data[], int nn,int isign)
{
		int i,j,m,n,mmax,istep;
		double wtemp,wr,wpr,wpi,wi,theta;
		double tempr,tempi;


		n=nn<<1;
		//n=nn;
		j=1;
		for(i=1;i<n;i+=2)
		{
		   if(j>i)
		   {
			   SWAP(data[j-1],data[i-1]);
			   SWAP(data[j],data[i]);
		   }
		   m=nn;

		   while(m>=2&&j>m)
		   {
				j-=m;
			   m>>=1;
			}
		   j+=m;
		}

		mmax=2;
		while(n>mmax)
		{
		   istep=mmax<<1;
		   theta=isign*(6.28318530717959/mmax);
		   wtemp=sin(0.5*theta);
		   wpr=-2.0*wtemp*wtemp;
		   wpi=sin(theta);
		   wr=1.0;
		   wi=0.0;
		   for(m=1;m<mmax;m+=2)
		   {
			   for(i=m;i<=n;i+=istep)
			   {
				   j=i+mmax;
				   tempr=wr*data[j-1]-wi*data[j];
				   tempi=wr*data[j]+wi*data[j-1];
					data[j-1]=data[i-1]-tempr;
				   data[j]=data[i]-tempi;
				   data[i-1]+=tempr;
				   data[i]+=tempi;
			   }
			   wr=(wtemp=wr)*wpr-wi*wpi+wr;
			   wi=wi*wpr+wtemp*wpi+wi;
		   }
		   mmax=istep;
		}
}

#define FRAME_NUMBER 19
JNIEXPORT jlong JNICALL Java_com_viash_voice_1assistant_speech_VoiceEnergyDetect_addData(JNIEnv *jniEnv, jclass jc, jbyteArray jdataArray, jint dataLen,jint isVoice)
{
  
  //frameLength defines the length of each frame and frameShift defines the overlapping part of neighbor frames
	 //as each data takes 2 bytes, length of data is dataLen/2
	 //here we separate pData into 19 segments, for eahc one, its length is set as frameLength=dataLen/20
	 int length = 2;
	 jint iArray[2];
	 jlong interval = 0;
	 //jintArray iarr = (*jniEnv)->NewIntArray(jniEnv,length);
	 int frameLength=dataLen/20, frameShift=dataLen/40, frameNumber=19;
	 int s_point=0,e_point=0;
	 float varThreshold= 0.0045;//0.0003;
	 int mark[FRAME_NUMBER]={0},mark1[FRAME_NUMBER-4]={0},mark2[FRAME_NUMBER-4]={0};
	 float mean=0,var[FRAME_NUMBER]={0};
	 //int voiceData[2000]={0};
     int i = 0,j = 0;
	 int pStartOffset = 0,pEndOffset = 0;
	 jbyte *pData = (*jniEnv)->GetByteArrayElements(jniEnv,jdataArray,0);
	 int len = (*jniEnv)->GetArrayLength(jniEnv,jdataArray); 
	 //LOGI("jbyteArray len = %d",i,len);

	 double fft_data[2048]={0};
	 double useful=0, unuseful1=0, unuseful2=0;

	 isVoice = 0;

	 //read data from memory
	 short *voiceData = (short *)malloc(sizeof(short)*dataLen/2);
	 memset(voiceData,0,sizeof(short)*dataLen/2);
	 for (i=0; i<dataLen/2; i++)
	 {
		 memcpy((char*)&voiceData[i],&pData[i*2],2);
		 //voiceData[i] = pData[i*2]<<8+ pData[i*2+1];
		 //LOGI("%d",voiceData[i]);
		 //LOGI("%0x  %0x",pData[i*2],pData[i*2+1]);
		 //voiceData[i]=voiceData[i]/65536;
	 }

	 for ( i=0; i<2048; i++)
		fft_data[i]=(double)voiceData[i]/32768;
	 fft(fft_data,1024,1);
	 for ( j=80; j<200; j++ )
		useful=useful+fabs(fft_data[j])/120;
	 for (j=801; j<1000; j++ )
		unuseful1=unuseful1+fabs(fft_data[j])/200;
	 for (j=0; j<20; j++ )
		unuseful2=unuseful2+fabs(fft_data[j])/20;
	 if ( useful > 2 * unuseful1 && useful > 1.5 * unuseful2 )
		isVoice=1;

	 //calculate the mean and the variances of each frame to judge the energy level
	 for ( i=0; i<frameNumber; i++ )
	 {
		 for ( j=0; j<frameLength; j++)
		 {
			 mean+=(float)(voiceData[ i*frameShift + j ])/frameLength;
		 }
		 mean=mean/65536;
         
		 for ( j=0; j<frameLength; j++)
		 {
			 var[i]+=( (float)voiceData[ i*frameShift + j ]/65536 -mean ) * ( (float)voiceData[ i*frameShift + j ]/65536 -mean );// / frameLength/ frameLength;
			 //LOGI("addData()22 var[%d] = %f mean = %f",i,var[i],mean);
		 }
  
		 if ( var[i] > varThreshold )
			 mark[i] = 1;
  
	 }
	 for ( i=0; i<frameNumber-4; i++ )
	 {
		 if ( mark[i]+mark[i+1]+mark[i+2]+mark[i+3]+mark[i+4] > 3 )
		 {
			 mark1[i]=1;
		 }
		 if ( mark[i]+mark[i+1]+mark[i+2]+mark[i+3]+mark[i+4] < 2 )
		 {
			 mark1[i]=-1;
		 }
			 
	 }
  
	 signed mm=-2;
	 for (i=0;i<frameNumber-4;i++)
	 {
	     //LOGI("addData()33 mark1[%d] = %d",i,mark1[i]);
		 if(mm==-1 && mark1[i]==1)
		 {
			 mm=1;
			 mark2[i]=1;
		 }
		 if( mm==1 && mark1[i]==-1)
		 {
			 mm=-1;
			 mark2[i]=2;
		 }
		 if(mm==-2 && mark1[i]==1)
		 {
			 mm=1;
			 mark2[i]=1;
			 //LOGI("addData()44");
		 }
		 if( mm==-2 && mark1[i]==-1)
		 {
			 mm=-1;
			 mark2[i]=20;
			 //LOGI("addData()55");
		 }
	 }
	 
	 //we choose the start point (s_point) as the first s_point
	 for (i=0;i<frameNumber-4;i++)
	 {
		 if ( mark2[i]==1 )
		 {
			 s_point=i+1;
			 break;
		 }
	 }
	 for (i=0;i<frameNumber-4;i++)
	 {
		 if ( mark2[i]==2 )
		 {
		     //LOGI("addData()55");
			 e_point=i+4;
		 }		 
	 }
	 //Leo Begin
	 if((lastEndPoint!= 19) && !( e_point==0 & s_point>0 ))
	 {
	    s_point = 0;
	 	e_point = 0;
		//LOGI("addData() last data and this data are not illegal,abandon.");
	 }
	 //Leo End
	 
	 //LOGI("addData()66 e_point = %d s_point = %d",e_point,s_point);
	 //if we have a start point and no end point, we set the end point to 19
	 if ( e_point==0 & s_point>0 )
			 e_point=19;
     lastEndPoint= e_point;//Leo

	 pStartOffset=(s_point-1)*frameShift*2;
	 pEndOffset=(e_point+1)*frameShift*2;
  
	 //if we have no s_point and e_point, it means there is no voice imported, 
	 //then we return 0 for both of pStartOffset and pEndOffset
	 if ( e_point==0 & s_point==0 )
	 {
		 pStartOffset=0;
		 pEndOffset=0;
	 }
     iArray[0] = pStartOffset;
     iArray[1] = pEndOffset;
  //   pStartOffset = (int)(pStartOffset<<16)+ pEndOffset;
  //   interval = (long)(pStartOffset<<32)+isVoice;//(long)(pStartOffset<<32)+pEndOffset;
     interval = (int)(pStartOffset<<16)+pEndOffset;
     //LOGI("addData()77 interval = %x  %d",interval,interval);
     if(isVoice == 1)
       interval = 0x100000000 + interval;
	 //LOGI("addData()77 iArray[0] = %d",iArray[0]);
	 //LOGI("addData()77 iArray[1] = %d",iArray[1]);
     //LOGI("addData()77 isVoice = %d",isVoice);
     //LOGI("addData()77 interval = %x  %d",interval,interval);
	 if(voiceData != NULL)
	 {
        free(voiceData);
		voiceData = NULL;
	 }
     //(*jniEnv)->SetIntArrayRegion(jniEnv,iarr,0,length,iArray);
	 (*jniEnv)->ReleaseByteArrayElements(jniEnv,jdataArray,pData,0);
	 return interval;
}

JNIEXPORT void JNICALL Java_com_viash_voice_1assistant_speech_VoiceEnergyDetect_stopDetect(JNIEnv *jniEnv, jclass jc)
{

}

#ifdef __cplusplus
}
#endif
#endif
