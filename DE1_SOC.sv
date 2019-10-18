module DE1_SOC (CLOCK_50, HEX0, HEX1, HEX2, HEX3, HEX4, HEX5, KEY, SW, LEDR, GPIO_0);

	output logic [6:0] HEX0, HEX1, HEX2, HEX3, HEX4, HEX5;
	output logic [4:0] LEDR;
	input logic [3:0] KEY;
	input logic CLOCK_50;
	input logic [9:0] SW;
	inout logic [35:0] GPIO_0;


	logic [31:0] clk;
	parameter whichClock = 22;
	//clock_divider cdiv (CLOCK_50, clk);
	assign clk[whichClock] = CLOCK_50;

	logic A;
	logic B;

	parameter WIDTH = 4;
	logic reset;
	logic [WIDTH:0] outputLight;
	logic [WIDTH-1:0] counter_num1, counter_num2;
	logic up, down;
	assign reset = SW[9];
	assign A = ~KEY[1];
	assign B = ~KEY[2];
	assign LEDR[4:0] = outputLight[4:0];
	assign GPIO_0[0] = A;
	assign GPIO_0[1] = B; 

	counter runCounter(.out(outputLight), .inc(up), .dec(down), .reset(reset), .clk(clk[whichClock]), .count_num1(counter_num1), .count_num2(counter_num2));
	display runDisplay(.out(outputLight), .clk(clk[whichClock]), .reset(reset), .counter_num1, .counter_num2, .HEX0, .HEX1, .HEX2, .HEX3, .HEX4, .HEX5);
	sensor runSensor(.clk(clk[whichClock]), .reset(reset), .C(A), .D(B), .enter(up), .exit(down));
	
endmodule 

 
module clock_divider (clock, divided_clocks);
	input logic clock;
	output logic [31:0] divided_clocks = 0;
	always_ff @(posedge clock) begin
		divided_clocks <= divided_clocks + 1;
	end
endmodule



module DE1_SOC_testbench();
	logic [6:0] HEX0, HEX1, HEX2, HEX3, HEX4, HEX5;
	logic [4:0] LEDR;
	logic [3:0] KEY;
	logic clk;
	logic [9:0] SW;
	tri [35:0]GPIO_0;
	 DE1_SOC dut (.CLOCK_50(clk), .HEX0, .HEX1, .HEX2, .HEX3, .HEX4, .HEX5, .KEY, .SW, .LEDR, .GPIO_0);
	 
	 // Set up the clock.
	 parameter CLOCK_PERIOD=100;
	 initial begin
	  clk <= 0;
	  forever #(CLOCK_PERIOD/2) clk <= ~clk;
	 end
	 // whichClock must be zero when rnning the simulation
	 // Set up the inputs to the design. Each line is a clock cycle.
	 initial begin
											  @(posedge clk);
		  SW[9] <= 1;                @(posedge clk);
		  SW[9] <= 0;                @(posedge clk);
		  KEY[1] <= 0; KEY[2] <= 0;  @(posedge clk);
		  KEY[1] <= 1; KEY[2]<=0;    @(posedge clk);
		  KEY[1] <= 1; KEY[2]<=1;    @(posedge clk);
											  @(posedge clk);
		  KEY[1] <= 0; KEY[2]<=1;    @(posedge clk);
											  @(posedge clk);
		  KEY[1] <= 0; KEY[2]<=0;    @(posedge clk);
											  @(posedge clk);
		  KEY[1] <= 0; KEY[2]<=1;    @(posedge clk);
											  @(posedge clk);
		  KEY[1] <= 1; KEY[2]<=1;    @(posedge clk);            
						
											  @(posedge clk);
		 KEY[1] <= 1; KEY[2]<=0;     @(posedge clk);      
											  @(posedge clk); 
		  KEY[1] <= 0; KEY[2]<=0;    @(posedge clk);
		  KEY[1] <= 0; KEY[2]<=0;                            @(posedge clk); 
		  KEY[1] <= 0; KEY[2]<=1;                            @(posedge clk);
		  KEY[1] <= 1; KEY[2]<=1;                           @(posedge clk); 
		  KEY[1] <= 1; KEY[2]<=0;    @(posedge clk);
		  KEY[1] <= 0; KEY[2]<=0;  @(posedge clk);
		//                             @(posedge clk);            
			$stop; // End the simulation.
	 end
endmodule

