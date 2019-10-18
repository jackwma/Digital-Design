module counter#(parameter WIDTH=4) (out, inc, dec, reset, clk, count_num1, count_num2);
	output logic [WIDTH:0] out;
	output logic [WIDTH-1:0] count_num1, count_num2;
	input logic inc, dec, reset, clk;
	 
	 //post: this block is to sure the counters will be able to increment the counters within the range correctly.
	 always_ff @(posedge clk) begin
		if(reset) begin // case for default start
			count_num1 <= 0;
			count_num2 <= 0;
			out <= 0;
		end
		else if(inc && out < 25) begin // within the range of 25 and incrementing
			out <= out + 1'b1;
			if(count_num1 < 9) begin // within 9 so that it doesn't use the second counter
				count_num1 <= count_num1 + 1'b1;
			end
			else begin //
				if(count_num2 < 9) begin
					count_num2 <= count_num2 + 1'b1;
				end
				else begin
					count_num2 <= 0;
					count_num1 <= 0;
				end
			end
		end
		else if(dec && out > 0) begin // the counter is positive so it is okay to decrement
			out <= out - 1'b1;
			if(count_num1 > 0) begin 
				count_num1 <= count_num1 - 1;
			end
			else begin
				if(count_num2 > 0) begin
					count_num2 <= count_num2 - 1;
				end
				else begin
					count_num2 <= 9;
					count_num1 <= 9;
				end
			end
		end
	  end 
endmodule


module counter_testbench#(parameter WIDTH=4) ();  
	logic [WIDTH:0] out;
	logic [WIDTH-1:0] count_num1, count_num2;
	logic inc, dec, reset, clk;
	 
	counter dut( .out, .inc, .dec, .reset, .clk, .count_num1, .count_num2);      
	// Set up the clock.  
	parameter CLOCK_PERIOD = 100;  
	initial begin
	 clk <= 1; 
	 forever #( CLOCK_PERIOD / 2 ) clk <= ~clk;  
	end      
	// Set up the inputs to the design.  Each line is a clock cycle.  
	initial begin                        
					                     @(posedge clk);    
		reset <= 1;					      @(posedge clk);    
		reset <= 0;                   @(posedge clk);                        
					  inc <= 1; dec <=0; @(posedge clk);                        
					  inc <= 1; dec <=0; @(posedge clk);                        
					  inc <= 0; dec <=1; @(posedge clk);                        
					  inc <= 1; dec <=0; @(posedge clk);                        
					  inc <= 1; dec <=0; @(posedge clk);                        
					  inc <= 0; dec <=1; @(posedge clk);                        
					  inc <= 0; dec <=1; @(posedge clk);                        
					  inc <= 1; dec <=0; @(posedge clk);                        
					  inc <= 1; dec <=0; @(posedge clk);                        
					  inc <= 1; dec <=0; @(posedge clk);                        
												@(posedge clk);  
		           inc <= 1; dec <=0; @(posedge clk);                        
												@(posedge clk);  
												@(posedge clk);
												@(posedge clk);  
					  inc <= 1; dec <=0; @(posedge clk);                        
												@(posedge clk);
												@(posedge clk);
												@(posedge clk);     
		$stop; // End the simulation.  
	end 
endmodule
