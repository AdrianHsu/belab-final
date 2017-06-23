sigma = 2;
sz = 10;
lp = linspace(-sz/2, sz/2, sz);
gF = exp( -lp .^ 2 / (2 * sigma^2));
gF = gF / sum(gF);
dataFt = conv(data,gF,'same');
diff = conv(dataFt, [1;-1], 'same');
diff2 = conv(dataFt, [-0.5;1;-0.5], 'same');
x = 5:496;
plot(x,dataFt(x)-400, x, diff(x), x, diff2(x)+50, x, zeros(492,1));
% text(61,dataFt(61)-400, 'P');
% text(83,dataFt(83)-400, 'Q');
% text(95,dataFt(95)-400, 'R');
% text(104,dataFt(104)-400,'S');
i = 4;
while i<=496
   i = i+1;
   if (abs(diff(i)) > 1)
      text(i,diff(i),'*');
      fprintf("%d, %f\n", i, diff(i));
      while (abs(diff(i)) > 1 && i < 496)
         i = i+1;
      end
   end
end

