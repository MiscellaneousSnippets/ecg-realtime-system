
import processing.serial.*; 
import processing.core.PApplet; 
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

// General Java Package
import java.math.*;


/************** Packet Validation  **********************/
private final int CESState_Init = 0;
private final int CESState_SOF1_Found = 1;
private final int CESState_SOF2_Found = 2;
private final int CESState_PktLen_Found = 3;

/*CES CMD IF Packet Format*/
private final int CES_CMDIF_PKT_START_1 = 0x0A;
private final int CES_CMDIF_PKT_START_2 = 0xFA;
private final int CES_CMDIF_PKT_STOP = 0x0B;

/*CES CMD IF Packet Indices*/
private final int CES_CMDIF_IND_LEN = 2;
private final int CES_CMDIF_IND_LEN_MSB = 3;
private final int CES_CMDIF_IND_PKTTYPE = 4;
private int CES_CMDIF_PKT_OVERHEAD = 5;

/************** Packet Related Variables **********************/

int ecs_rx_state = 0;                                        // To check the state of the packet
int CES_Pkt_Len;                                             // To store the Packet Length Deatils
int CES_Pkt_Pos_Counter, CES_Data_Counter;                   // Packet and data counter
int CES_Pkt_PktType;                                         // To store the Packet Type
char CES_Pkt_Data_Counter[] = new char[1000];                // Buffer to store the data from the packet
char CES_Pkt_ECG_Counter[] = new char[4];                    // Buffer to hold ECG data
char CES_Pkt_Resp_Counter[] = new char[4];                   // Respiration Buffer
char CES_Pkt_SpO2_Counter_RED[] = new char[4];               // Buffer for SpO2 RED
char CES_Pkt_SpO2_Counter_IR[] = new char[4];                // Buffer for SpO2 IR
int pSize = 1000;                                            // Total Size of the buffer
int arrayIndex = 0;                                          // Increment Variable for the buffer
 float time = 0;                                              // X axis increment variable

// Buffer for ecg,spo2,respiration,and average of thos values
 float[] xdata = new float[pSize];
 float[] ecgdata = new float[pSize];
 float[] respdata = new float[pSize];
 float[] bpmArray = new float[pSize];
 float[] ecg_avg = new float[pSize];                          
 float[] resp_avg = new float[pSize];
 float[] spo2data = new float[pSize];
 float[] spo2Array_IR = new float[pSize];
 float[] spo2Array_RED = new float[pSize];
 float[] rpmArray = new float[pSize];
 float[] ppgArray = new float[pSize];

/************** Graph Related Variables **********************/

double maxe, mine, maxr, minr, maxs, mins;             // To Calculate the Minimum and Maximum of the Buffer
double ecg, resp, spo2_ir, spo2_red, spo2, redAvg, irAvg, ecgAvg, resAvg;  // To store the current ecg value
double respirationVoltage=20;                          // To store the current respiration value
boolean startPlot = true;                             // Conditional Variable to start and stop the plot


int step = 0;
int stepsPerCycle = 100;
int lastStepTime = 0;
boolean clockwise = true;
float scale = 5;

/************** Port Related Variables **********************/

Serial port = null;                                     // Oject for communicating via serial port
String[] comList;                                       // Buffer that holds the serial ports that are paired to the laptop
char inString = '\0';                                   // To receive the bytes from the packet
String selectedPort;                                    // Holds the selected port number

/************** Logo Related Variables **********************/


boolean gStatus;                                        // Boolean variable to save the grid visibility status

int nPoints1 = pSize;
int totalPlotsHeight=0;
int totalPlotsWidth=0;
int heightHeader=100;
int updateCounter=0;

boolean is_raspberrypi=false;

int global_hr;
int global_rr;
float global_temp;
int global_spo2;

int global_test=0;

boolean ECG_leadOff,spo2_leadOff;
boolean ShowWarning = true;
boolean ShowWarningSpo2=true;


Socket socket;
public void setup(){
  
  try{
   socket = IO.socket("http://192.168.43.27:3000");
  }catch(Exception e){
   println("no connection to socket"); 
  }
  socket.connect();
    for (int i=0; i<pSize; i++) 
  {
    time = time + 1;
    xdata[i]=time;
    ecgdata[i] = 0;
    respdata[i] = 0;
    ppgArray[i] = 0;
  }
  time = 0;

  
    startSerial("/dev/ttyAMA0",57600);
  

}

public void draw() 
{

}

void startSerial(String startPortName, int baud)
{
  try
  {
      port = new Serial(this,startPortName, baud);
      port.clear();
      startPlot = true;
  }
  catch(Exception e)
  {
      System.out.println("serial problem");
  }
}

//Main Driver !!
void serialEvent (Serial blePort) 
{
  inString = blePort.readChar();
  ecsProcessData(inString);
}

//the method that process data
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void ecsProcessData(char rxch)
{
  switch(ecs_rx_state)
  {
  case CESState_Init:
    if (rxch==CES_CMDIF_PKT_START_1)
      ecs_rx_state=CESState_SOF1_Found;
    break;

  case CESState_SOF1_Found:
    if (rxch==CES_CMDIF_PKT_START_2)
      ecs_rx_state=CESState_SOF2_Found;
    else
      ecs_rx_state=CESState_Init;                    //Invalid Packet, reset state to init
    break;

  case CESState_SOF2_Found:
    ecs_rx_state = CESState_PktLen_Found;
    CES_Pkt_Len = (int) rxch;
    CES_Pkt_Pos_Counter = CES_CMDIF_IND_LEN;
    CES_Data_Counter = 0;
    break;

  case CESState_PktLen_Found:
    CES_Pkt_Pos_Counter++;
    if (CES_Pkt_Pos_Counter < CES_CMDIF_PKT_OVERHEAD)  //Read Header
    {
      if (CES_Pkt_Pos_Counter==CES_CMDIF_IND_LEN_MSB)
        CES_Pkt_Len = (int) ((rxch<<8)|CES_Pkt_Len);
      else if (CES_Pkt_Pos_Counter==CES_CMDIF_IND_PKTTYPE)
        CES_Pkt_PktType = (int) rxch;
    } else if ( (CES_Pkt_Pos_Counter >= CES_CMDIF_PKT_OVERHEAD) && (CES_Pkt_Pos_Counter < CES_CMDIF_PKT_OVERHEAD+CES_Pkt_Len+1) )  //Read Data
    {
      if (CES_Pkt_PktType == 2)
      {
        CES_Pkt_Data_Counter[CES_Data_Counter++] = (char) (rxch);          // Buffer that assigns the data separated from the packet
      }
    } else  //All  and data received
    {
      if (rxch==CES_CMDIF_PKT_STOP)
      {     
        CES_Pkt_ECG_Counter[0] = CES_Pkt_Data_Counter[0];
        CES_Pkt_ECG_Counter[1] = CES_Pkt_Data_Counter[1];


        CES_Pkt_Resp_Counter[0] = CES_Pkt_Data_Counter[2];
        CES_Pkt_Resp_Counter[1] = CES_Pkt_Data_Counter[3];

        CES_Pkt_SpO2_Counter_IR[0] = CES_Pkt_Data_Counter[4];
        CES_Pkt_SpO2_Counter_IR[1] = CES_Pkt_Data_Counter[5];
        CES_Pkt_SpO2_Counter_IR[2] = CES_Pkt_Data_Counter[6];
        CES_Pkt_SpO2_Counter_IR[3] = CES_Pkt_Data_Counter[7];

        CES_Pkt_SpO2_Counter_RED[0] = CES_Pkt_Data_Counter[8];
        CES_Pkt_SpO2_Counter_RED[1] = CES_Pkt_Data_Counter[9];
        CES_Pkt_SpO2_Counter_RED[2] = CES_Pkt_Data_Counter[10];
        CES_Pkt_SpO2_Counter_RED[3] = CES_Pkt_Data_Counter[11];

        float Temp_Value = (float) ((int) CES_Pkt_Data_Counter[12]| CES_Pkt_Data_Counter[13]<<8)/100;                // Temperature
        // BP Value Systolic and Diastolic
        
        int global_RespirationRate = (int) (CES_Pkt_Data_Counter[14]);
         int global_spo2= (int) (CES_Pkt_Data_Counter[15]);
         int global_HeartRate = (int) (CES_Pkt_Data_Counter[16]);
         
        int BP_Value_Sys = (int) CES_Pkt_Data_Counter[17];
        int BP_Value_Dia = (int) CES_Pkt_Data_Counter[18];
        
        int leadstatus =  CES_Pkt_Data_Counter[19];
        leadstatus &= 0x01; 
        if(leadstatus== 0x01) ECG_leadOff = true;  
        else ECG_leadOff = false;
        
         leadstatus =  CES_Pkt_Data_Counter[19];
        leadstatus &= 0x02; 
        if(leadstatus == 0x02) spo2_leadOff = true;
        else spo2_leadOff = false;
        

        int data1 = CES_Pkt_ECG_Counter[0] | CES_Pkt_ECG_Counter[1]<<8; //reversePacket(CES_Pkt_ECG_Counter, CES_Pkt_ECG_Counter.length-1);
        data1 <<= 16;
        data1 >>= 16;
        ecg = (double) data1/(Math.pow(10, 3));

        int data2 = CES_Pkt_Resp_Counter[0] | CES_Pkt_Resp_Counter[1] <<8; //reversePacket(CES_Pkt_ECG_Counter, CES_Pkt_ECG_Counter.length-1);
        data2 <<= 16;
        data2 >>= 16;
        resp = (double) data2/(Math.pow(10, 3));

        int data3 = reversePacket(CES_Pkt_SpO2_Counter_IR, CES_Pkt_SpO2_Counter_IR.length-1);
        spo2_ir = (double) data3;

        int data4 = reversePacket(CES_Pkt_SpO2_Counter_RED, CES_Pkt_SpO2_Counter_RED.length-1);
        spo2_red = (double) data4;

        ecg_avg[arrayIndex] = (float)ecg;
        ecgAvg = averageValue(ecg_avg);
        ecg = (ecg_avg[arrayIndex] - ecgAvg);

        spo2Array_IR[arrayIndex] = (float)spo2_ir;
        spo2Array_RED[arrayIndex] = (float)spo2_red;
        redAvg = averageValue(spo2Array_RED);
        irAvg = averageValue(spo2Array_IR);
        spo2 = (spo2Array_IR[arrayIndex] - irAvg);

        resp_avg[arrayIndex]= (float)resp;
        resAvg =  averageValue(resp_avg);
        resp = (resp_avg[arrayIndex] - resAvg);

        time = time+1;
        xdata[arrayIndex] = time;

        ecgdata[arrayIndex] = (float)ecg;
        respdata[arrayIndex]= (float)resp;
        spo2data[arrayIndex] = (float)spo2;
        bpmArray[arrayIndex] = (float)ecg;
        rpmArray[arrayIndex] = (float)resp;
        ppgArray[arrayIndex] = (float)spo2;
       
        arrayIndex++;
        updateCounter++;

        if(updateCounter==100)
        {
          
            global_temp=Temp_Value;
            
       
          
            //Handle packet transmission//////////////////////////////////////
            org.json.JSONObject rawData = new org.json.JSONObject();
            org.json.JSONArray ecgValues = new org.json.JSONArray(); 
            org.json.JSONArray spo2Values = new org.json.JSONArray(); 
            org.json.JSONArray respValues = new org.json.JSONArray(); 
            
            for(int i =0 ; i<ecgdata.length; i++){
              try{
              ecgValues.put(ecgdata[i]);
              spo2Values.put(spo2data[i]);
              respValues.put(respdata[i]);
              }catch(Exception e){
                e.printStackTrace();
              }
            }
            try{
              //Send Global Data
              rawData.put("global_temp",global_temp);
              rawData.put("global_HeartRate",global_HeartRate);
              rawData.put("global_spo2",global_spo2);
              rawData.put("global_RespirationRate",global_RespirationRate);
              
              //Send Array Data
              rawData.put("ecg_values",ecgValues);
              rawData.put("spo2_values",spo2Values);
              rawData.put("resp_values",respValues);
          
              socket.emit("foo", rawData);
            
        
                }catch(Exception e){
                    System.out.println("json problem");
                }
                   
         
          updateCounter=0;
        }
        
        if (arrayIndex == pSize)
        {  
          arrayIndex = 0;
          time = 0;
        }       



        ecs_rx_state=CESState_Init;
      } else
      {
        ecs_rx_state=CESState_Init;
      }
    }
    break;

  default:
    break;
  }
}

/*********************************************** Recursive Function To Reverse The data *********************************************************/

public int reversePacket(char DataRcvPacket[], int n)
{
  if (n == 0)
    return (int) DataRcvPacket[n]<<(n*8);
  else
    return (DataRcvPacket[n]<<(n*8))| reversePacket(DataRcvPacket, n-1);
}

/*************** Function to Calculate Average *********************/
double averageValue(float dataArray[])
{

  float total = 0;
  for (int i=0; i<dataArray.length; i++)
  {
    total = total + dataArray[i];
  }
  return total/dataArray.length;
}
