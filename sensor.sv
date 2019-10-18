module sensor (clk, reset, C, D, enter, exit);
	input logic clk, reset, C, D;
	output logic enter, exit;
	
	//creating a ps, ns case for these 4 states
	enum {EMPTY, A, B, AB} ps, ns;
	
	//post: this block is to ensure that the inputs will be able to change states within correctly.
	always_comb begin
		case(ps)	
		EMPTY: // EMPTY case
			if(C&~D) ns = A;
			else if(~C&D) ns = B;
			else ns = EMPTY;
		A: // case for A
			if(C&~D) ns = A;
			else if(C&D) ns = AB;
			else ns = EMPTY;		// Dont care, go to EMPTY
		B: // case for B
			if(~C&D) ns = B;
			else if(C&D) ns = AB;
			else ns = EMPTY;		// Dont care, go to EMPTY
		AB: // case for A and B
			if(C&D) ns = AB;
			else if(C&~D) ns = A;
			else if(~C&D) ns = B;
			else ns = EMPTY;
		endcase
	end
	
	//post: block to record inputs; will be able to record the input and change its states with enter and exit. 
	always_comb begin
		case(ps) 
		AB:
			if(~C&D)
				begin 
					enter = 1'b1;
					exit = 1'b0;
				end
			else if(C&~D)
				begin
					exit = 1'b1;
					enter = 1'b0;
				end
			else 
				begin
					enter = 1'b0;
					exit = 1'b0;
				end
		default: 
			begin 
				enter = 1'b0;
				exit = 1'b0;
			end
		endcase
	end
	
	//DFF to eliminate metability
	always_ff @(posedge clk) begin
		if(reset)
			ps <= EMPTY;
		else
			ps <= ns;
	end
endmodule

module sensor_testbench();
	logic clk, reset, a, b, enter, exit;
	
	sensor dut(.clk, .reset, .C(a), .D(b), .enter, .exit);
	
	parameter CLOCK_PERIOD = 100;
	initial begin
		clk <= 0;
		forever #(CLOCK_PERIOD/2) clk <= ~clk;
	end
	
	initial begin
		reset <= 1; a <= 1; b <= 1; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 0; b <= 0; @(posedge clk);
		@(posedge clk);
		@(posedge clk);		
		reset <= 0; 	 @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 1; b <= 0; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 1; b <= 1; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 0; b <= 0; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 1; b <= 0; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 0; b <= 0; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 1; b <= 1; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 1; b <= 1; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 1; b <= 0; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		a <= 0; b <= 0; @(posedge clk);
		@(posedge clk);
		@(posedge clk);
		$stop;
	end
	
endmodule 
	
	