import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class cop4520_hw4_2 {


	public static void main(String[] args) throws InterruptedException {
		
		int i;
		int tempSensors = 8;
		int duration = 24; /*in terms of hours*/
		long starttime, endtime, executiontime;	 
		long beforeUsedMem, afterUsedMem,actualMemUsed;
		
		starttime = System.currentTimeMillis();
		beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		
		AtomicBoolean finished = new AtomicBoolean(false); /* used for finishing when counter is done*/
		
		TempModule record = new TempModule(finished);	
		
		Thread[] thread = new Thread[tempSensors];
		
		for(i = 0; i < tempSensors; i++) {
			thread[i] = new Thread(new Sensor(record, finished, duration), "Sensor " + i);
		}
			
		for(i = 0; i < tempSensors; i++) {
			thread[i].start();
		}
			
		for(i = 0; i < tempSensors; i++) {
			thread[i].join();
		}
		
		afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		endtime = System.currentTimeMillis();
		
		executiontime = endtime - starttime;
		actualMemUsed=afterUsedMem-beforeUsedMem;
		
		System.out.println("\n   ___________________________________________");
		System.out.println("\t Exetution time: " + (int)(executiontime)/100 + "ms");
		System.out.println("\t Memory usage: " + actualMemUsed);
		System.out.println("   ___________________________________________");
		
		
	}
}

//Guest class which used for going into room
class Sensor implements Runnable {
		
	AtomicBoolean finished;
	private AtomicInteger duration;
	private TempModule record;
		
	public Sensor(TempModule record, AtomicBoolean finished, int duration) {
		this.record = record;
		this.duration = new AtomicInteger(duration);
		this.finished = finished;
	}
		
	public void run() {
		while(!finished.get()) {						
			try {
				record.operation(record, duration);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}



class tempReading {
		
	int temp; /*switch to double if necessary*/
	int start;
	int end;
	tempReading next;
	
	public tempReading(int temp, int start, int end) {
		this.temp = temp;
		this.start = start;
		this.end = end;
		this.next = null;
	}
}


class TempModule  {
		
	int num = 0;
	tempReading head;
	Random rand = new Random(); 
	Lock lock = new ReentrantLock();
	AtomicInteger temp = new AtomicInteger();
	AtomicInteger start = new AtomicInteger(-1);
	AtomicInteger end = new AtomicInteger(0);
	public AtomicInteger hour = new AtomicInteger(0);
	public AtomicInteger counter = new AtomicInteger(0); /*being used to detect time*/
	public AtomicBoolean finished = new AtomicBoolean(false);
		
	public TempModule (AtomicBoolean finished) {
		head = new tempReading(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		head.next = new tempReading(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		this.finished = finished;
	}
		
	public void add(int temp, int start, int end) {
			
		lock.lock();
		
		try {
			if(isEmpty()) {
				tempReading newNode = new tempReading(temp, start, end);
				head = newNode;
				head.next = null;
				counter.incrementAndGet();
			}
			else {
				tempReading pred = head;
								
				while(pred.next != null && pred.next.start < start) {
					pred = pred.next;	
				}
					
				tempReading newNode = new tempReading(temp, start, end);
				newNode.next = pred.next;
				pred.next = newNode;				
				counter.incrementAndGet();
			}
				
				
		}finally {
			lock.unlock();
		}
	}

	
	public boolean isEmpty() {
			
		lock.lock();
			
		try {
			return (head == null) ? true : false; 
				
		} finally {
			lock.unlock();
		}
			
	}
			

	public void display(int s_interval, int e_interval) {
		
		lock.lock();
		
		try {				
			if(isEmpty()) {	
				System.out.printf("No records for following interval.\n");
			}				
			else {
				tempReading pred = head;
					
				while(pred.next != null && pred.next.start >= s_interval && pred.next.start <= e_interval) {

					if(pred.start != Integer.MIN_VALUE && pred.start != Integer.MAX_VALUE) {
						 if(hour.get() == 0)
							System.out.printf(" \tIntervals: %d - %d    Temp: %dF\n", pred.start, pred.end, pred.temp);
					     else
					       	System.out.printf(" \tIntervals: %d - %d    Temp: %dF\n", pred.start%60, pred.end%60, pred.temp);
					}
						
					pred = pred.next;	
				}	
				
			}
		} finally {
			lock.unlock();
		}
		
	}
	
	
	public void lowest() {	
		
		lock.lock();
		
		try {				
			if(!isEmpty()) {
				
				ArrayList<Integer> lowest = new ArrayList<Integer>();
				
				tempReading pred = head;
				
				while(pred.next != null) {

					if(pred.temp != Integer.MIN_VALUE && pred.temp != Integer.MAX_VALUE) 
						lowest.add(pred.temp);
						
					pred = pred.next;	
				}	
				
				Collections.sort(lowest);
				
				int i = 0;
				for(int temp: lowest){
					if(i < 5)
						System.out.printf(" \t Lowest Temperatures: %dF\n", temp);
					i++;
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	
	public void highest() {
		
		lock.lock();
		
		try {				
			if(!isEmpty()) {
				
				ArrayList<Integer> highest = new ArrayList<Integer>();
				
				tempReading pred = head;
				
				while(pred.next != null) {

					if(pred.temp != Integer.MIN_VALUE && pred.temp != Integer.MAX_VALUE) 
						highest.add(pred.temp);
						
					pred = pred.next;	
				}	
				
				Collections.sort(highest);
				
				int i = 0;
				for(int temp: highest){
					if(i >= (highest.size() - 5) && i < highest.size())
						System.out.printf(" \t Highest Temperatures: %dF\n", temp);
					i++;
				}
			}
		} finally {
			lock.unlock();
		}
		
	}

	
	public void tempDifference() {
		
		lock.lock();
		
		try {				
			if(!isEmpty()) {
				
				int i = 0, j = 0;
				int start = 0, end = 0, diff;
				tempReading pred = head;
				tempReading curr = head.next;
				int[][] differences = new int[50][3];
				
				while(curr != null && j < 50) {
					
					if(pred.temp != Integer.MIN_VALUE && curr.temp != Integer.MAX_VALUE) {
						start = pred.temp;
						
						while(curr.next != null && i < 9) {
							curr= curr.next;
							i++;
						}
						
						end = curr.temp;
						
						diff = start - end;
						
						differences[j][0] = diff; 
						differences[j][1] = pred.start; 
						differences[j][2] =	curr.start;	
						j++;
								
					}	
					pred = pred.next;
					curr = curr.next; 
					
				}
				
				i = 0;
				
				int max = differences[0][i];
				
		        for (j = 1; j < 50; j++)
		        {
		        	if (differences[j][0] > max) 
		        	{
		        		max = differences[j][0];
		        		start =differences[j][1];
		        		end = differences[j][2];
		        	}
		        }
		        if(hour.get() == 0)
		        	System.out.printf(" \t Start Time: %d End Time: %d Difference: %dF\n", start, end, max);
		        else
		        	System.out.printf(" \t Start Time: %d End Time: %d Difference: %dF\n", start%60, end%60, max);
				
			}
		} finally {
			lock.unlock();
		}
		
	}

	
	public void operation(TempModule record, AtomicInteger duration) throws InterruptedException {

		lock.lock();
		
		try {
			temp.set(rand.nextInt(170) - 100);
			start.getAndIncrement();
			end.getAndIncrement();
			
			record.add(temp.get(), start.get(), end.get());
			
			if(counter.get() % 60 == 0) {
				
				System.out.println("\n\n   ======================================================");
				System.out.println("                Atmospheric Temperature Module       ");
				System.out.println("   ======================================================");
				System.out.println("   ______________________________________________________");
				System.out.printf("    Time:  %d hour\n", hour.get());
				System.out.println("   ______________________________________________________");
				System.out.printf("    Records:  \n");
				
				if(hour.get() == 0)
					record.display(0, 60);
				else
					record.display(60*hour.get(), 120*hour.get());
				
				System.out.println("   ______________________________________________________");
				System.out.println("   Lowest Temperature Record: ");
				record.lowest();
				System.out.println("   ______________________________________________________");
				
				System.out.println("   Highest Temperature Record: ");
				record.highest();
				System.out.println("   ______________________________________________________");
				
				System.out.println("   Largest temperature difference:");
				record.tempDifference();
			
				head = null;
				hour.getAndIncrement();
			}
								
			if(hour.get() == duration.get()) 
				finished.set(true);
		}
		finally {
			lock.unlock();
		}
	}
}
