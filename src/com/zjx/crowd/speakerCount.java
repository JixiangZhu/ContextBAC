package com.zjx.crowd;

import java.util.Vector;

import com.zjx.featureExtraction.mfccFeatureExtraction;

public class speakerCount {
	class Speaker
	{
		double mfcc[];//mfcc vector
		short seg_signal[];//segment the clip into smaller segments of equal length
		int sex;//male = 0;female = 1
	}
	//Crowd++ algorithm
	public int Count(short inputSignal[],double samplingRate)
	{	
		//the number of speaker
		int num = 0;
		//the number of all sample
		int sam_num = (int)(inputSignal.length/(samplingRate*3));
		//speaker class
		Speaker[] speaker;
		speaker = new Speaker[sam_num];
		//threshold
		double dis_min = 0.85;
		double dis_max = 0.6;
		//�������ڲ�ͬ�˵���ƵƬ�εļ���
		Vector<Speaker> C = new Vector<Speaker>();
		//�����˵���ƵƬ�εļ���
		Vector<Speaker> acf = new Vector<Speaker>();
		
		mfccFeatureExtraction mfccfe = new mfccFeatureExtraction();
		
		//init speaker
		for(int i=0;i<sam_num;i++)
		{
			speaker[i] = new Speaker();
			speaker[i].mfcc = new double[mfccfe.numCepstra-1];
			speaker[i].seg_signal = new short[(int)samplingRate*3];
			speaker[i].sex = 0;
		}
		
		//get mfcc vector
		int k = 0;
		for(int i = 0 ; i<inputSignal.length ; i+=samplingRate*3)
		{
			//the NO of speaker segment
			k = (int)(i/(samplingRate*3));
			if(inputSignal.length-k*(samplingRate*3)<(samplingRate*3))
			{
				break;
			}
			//segment inputSignal
			System.arraycopy(inputSignal, i, speaker[k].seg_signal , 0, (int)samplingRate*3);
			//�ж��Ƿ�Ϊ����
			double pitch = getPitch(speaker[k],(int)samplingRate); 
			if(pitch>=100 && pitch<=221)
			{
				//get speaker[k] mfcc vector(2-20)
				System.arraycopy(mfccfe.process(speaker[k].seg_signal, samplingRate), 1, speaker[k].mfcc , 0, 19);
				//�ж��Ա�
				if(pitch<=160)
				{
					speaker[k].sex = 0;
				}
				else
				{
					speaker[k].sex = 1;
				}
				//���˵�Ƭ�μӵ�acf������
				acf.add(speaker[k]);
			}
			//û��������Ƭ����,���������Ƭ��
			else
			{
				continue;
			}
			//speaker[k].mfcc = mfccfe.process(speaker[k].seg_signal, samplingRate);
		}
		
		int index = 1;
		if(acf.size()==0)
		{
			return 0;
		}
		else
		{
			C.add(acf.elementAt(0));
			while(index < acf.size())
			{
				//�ж��Ƿ�ɨ�����
				int flag=0;
				for(int i = 0 ;i<C.size();i++)
				{
					double tri;
					tri = sim(C.elementAt(i),acf.elementAt(index));
					//mfcc��������������ͬһ���Ա�,�������Ƭ��
					if(tri > dis_min && C.elementAt(i).sex==acf.elementAt(index).sex )
					{
						break;
					}
					//mfcc������ͬ�������ڲ�ͬ�Ա𣬼������Ƭ��
					else if(tri<dis_max ||  C.elementAt(i).sex!=acf.elementAt(index).sex)
					{
						//���ɨ��������C��û��
						if(flag == C.size()-1)
						{
							C.add(acf.elementAt(index));
							break;
						}
						//ɨ��ֵ��1
						flag++;
						continue;
					}
				}
				index++;
			}
			num = C.size();
			return num;
		}
	}
	//Cosine Similarity
	public double sim(Speaker speaker1,Speaker speaker2)
	{
		double result;
		double d_x = 0;
		double d_y = 0;
		double d_xy = 0;
		
		for(int i = 0;i<speaker1.mfcc.length;i++)
		{
			d_x += speaker1.mfcc[i]*speaker1.mfcc[i];
		}
		d_x = Math.sqrt(d_x);
		
		for(int i = 0;i<speaker2.mfcc.length;i++)
		{
			d_y += speaker2.mfcc[i]*speaker2.mfcc[i];
		}
		d_y = Math.sqrt(d_y);
		
		for(int i = 0;i<speaker1.mfcc.length;i++)
		{
			d_xy += speaker1.mfcc[i]*speaker2.mfcc[i]; 
		}
		
		result = d_xy/(d_x*d_y);
		
		return result;
	}
	//getPitch
	public double getPitch(Speaker speaker,int samplingRate)
	{
		//acf����
		double acf[];
		acf = acf(speaker,samplingRate);
		//��Ƶ
		double pitch = 0;
		//��һ������ֵ���λ��
		int maxAcfAd = 0;
		//ʵʱ����ֵ���ֵ
		double maxTemp = 0;
		//get Maxpoint
		for(int i = 0;i<(samplingRate*3)/10;i++)
		{
			if(i==0)
			{
				continue;
			}
			else
			{
				if(maxTemp>=acf[i])
				{
					continue;
				}
				else
				{
					maxTemp = acf[i];
					maxAcfAd = i;
				}
			}
		}
		pitch = samplingRate/(maxAcfAd*10);
		
		return pitch;
	}
	//acf:R(k)=sum(x[n]x[n+k]),n=0����N-k-1;k=0����samplingRate*3
	public double[] acf(Speaker speaker,int samplingRate )
	{
		double acf[];
		acf = new double[(samplingRate*3)/10];
		//k is lag
		for(int k = 0 ; k < samplingRate*3 ; k+=10)
		{
			double sum = 0;
			for(int m = 50 ; m<(samplingRate*3-k-1) ; m+=10)
			{
				sum += (speaker.seg_signal[m])*(speaker.seg_signal[m+k]);
			}
			acf[k/10] = sum;
		}
		return acf;
	}
}

