module display #(parameter WIDTH=4) (out, clk, reset, counter_num1, counter_num2, HEX0, HEX1, HEX2, HEX3, HEX4, HEX5);
	 input logic clk, reset;
	 input logic [WIDTH:0] out;
	 input logic [WIDTH-1:0] counter_num1, counter_num2;
	 output logic [6:0] HEX0, HEX1, HEX2, HEX3, HEX4, HEX5;

	 
	 //parameters for easier input 
	 //so I don't have to write this over and over again. Simplifies the process.
	 parameter  
		  zero  = 7'b1000000,
		  one  = 7'b1111001,
		  two  = 7'b0100100,
		  three  = 7'b0110000,
		  four  = 7'b0011001,
		  five  = 7'b0010010,
		  six  = 7'b0000010,
		  seven  = 7'b1111000,
		  eight  = 7'b0000000,
		  nine  = 7'b0010000,
		  off  = 7'b1111111;
		  
	 //post: this block is to ensure that the display will be able to display the different states correctly.
	 always @(*) begin
		case(out)
			5'b00000: // case for empty 
				begin
					if(counter_num2 == 4'b0000)
						HEX5 = 7'b0000110;
						HEX4 = 7'b1101010;
						HEX3 = 7'b0001100;
						HEX2 = 7'b0000111;
						HEX1 = 7'b0010001;
						HEX0 = off;
				end
			5'b11001: // case for full
				begin
					if(counter_num2 == 4'b0010)
						HEX5 = 7'b0001110;
						HEX4 = 7'b1000001;
						HEX3 = 7'b1000111;
						HEX2 = 7'b1000111;
						HEX1 = off;
						HEX0 = off;
				end
			default: //default case
				begin
				  HEX5 = off;
				  HEX4 = off;
				  HEX3 = off;
				  HEX2 = off;
				  case(counter_num1) 
						4'b0000: HEX0 = zero;
						4'b0001: HEX0 = one;
						4'b0010: HEX0 = two;
						4'b0011: HEX0 = three;
						4'b0100: HEX0 = four;
						4'b0101: HEX0 = five;
						4'b0110: HEX0 = six;
						4'b0111: HEX0 = seven;
						4'b1000: HEX0 = eight;
						4'b1001: HEX0 = nine;
						default: HEX0 = off;
				  endcase
				  case(counter_num2) 
						4'b0000: HEX1 = zero;
						4'b0001: HEX1 = one;
						4'b0010: HEX1 = two;
						4'b0011: HEX1 = three;
						4'b0100: HEX1 = four;
						4'b0101: HEX1 = five;
						4'b0110: HEX1 = six;
						4'b0111: HEX1 = seven;
						4'b1000: HEX1 = eight;
						4'b1001: HEX1 = nine;
						default: HEX1 = off;
				  endcase
				end
	  endcase
	end
endmodule


module display_testbench #(parameter WIDTH=4) ();
	 logic [WIDTH:0] out;
	 logic [WIDTH-1:0] count_num1, count_num2;
	 logic [6:0] HEX0, HEX1, HEX2, HEX3, HEX4, HEX5;
	 logic clk, reset;
	 
	 display dut(.clk, .reset, .counter_num1(count_num1), .counter_num2(count_num2), .HEX0, .HEX1, .HEX2, .HEX3, .HEX4, .HEX5);
	 
	 initial begin
			count_num2 <= 0;
			out <= 0; count_num1 <= 0; #10;
			out <= 1; count_num1 <= 1; #10;
			out <= 2; count_num1 <= 2; #10;
			out <= 3; count_num1 <= 3; #10;
			out <= 4; count_num1 <= 4; #10;
			out <= 5; count_num1 <= 5; #10;
			out <= 6; count_num1 <= 6; #10;
			out <= 7; count_num1 <= 7; #10;
			out <= 8; count_num1 <= 8; #10;
			out <= 9; count_num1 <= 9; #10;
		
			out <= 10; count_num1 <= 0; count_num2 <= 1; #10;
			out <= 11; count_num1 <= 1; count_num2 <= 1; #10;
			out <= 12; count_num1 <= 2; count_num2 <= 1; #10;
			out <= 13; count_num1 <= 3; count_num2 <= 1; #10;
			out <= 14; count_num1 <= 4; count_num2 <= 1; #10;
			out <= 15; count_num1 <= 5; count_num2 <= 1; #10;
			out <= 16; count_num1 <= 6; count_num2 <= 1; #10;
			out <= 17; count_num1 <= 7; count_num2 <= 1; #10;
			out <= 18; count_num1 <= 8; count_num2 <= 1; #10;
			out <= 19; count_num1 <= 9; count_num2 <= 1; #10;

			out <= 20; count_num1 <= 0; count_num2 <= 2; #10;
			out <= 21; count_num1 <= 1; count_num2 <= 2; #10;
			out <= 22; count_num1 <= 2; count_num2 <= 2; #10;
			out <= 23; count_num1 <= 3; count_num2 <= 2; #10;
			out <= 24; count_num1 <= 4; count_num2 <= 2; #10;
			out <= 25; count_num1 <= 5; count_num2 <= 2; #10;

			
	 end
endmodule 