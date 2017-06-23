function wave = getBeat(data)
sigma = 2;
sz = 10;
lp = linspace(-sz/2, sz/2, sz);
gF = exp( -lp .^ 2 / (2 * sigma^2));
gF = gF / sum(gF);
dataFt = conv(data,gF,'same');
diff = conv(dataFt, [1;-1], 'same');
diff2 = conv(dataFt, [-0.5;1;-0.5], 'same');
max1 = 0.3 * std(diff2);
max2 = 0.3 * std(diff2);
startIndex = 0;
endIndex = 0;
i = 10; stop = false;
while i<496 && ~stop
   while diff2(i) > max1
      max1 = diff2(i);
      startIndex = i;
      i = i+1;
      stop = true;
   end
   i = i+1;
end
stop = false;
while i<496 && ~stop
   while diff2(i) > max2
      max2 = diff2(i);
      endIndex = i;
      i = i+1;
      stop = true;
   end
   i = i+1;
end
fprintf('start = %d, end = %d\n', startIndex, endIndex);
wave = dataFt(startIndex-1:endIndex-1);