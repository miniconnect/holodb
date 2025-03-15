pkg load statistics

%N = 100
N = 100
%P = 0.5
P = 0.5

x = [0:0.001:1]

f1 = binoinv(x, N, P)

f2 = norminv(x, N * P, sqrt(N * P * (1 - P)))

%f3 = (((x - 0.5)*1.2).^13 * 5000) + ((x - 0.5) * 15) + 50
f3 = (((x - 0.5)*1.2).^13 * N * N) + ((x - 0.5) * sqrt(N * P)) + (N * P)

plot(x, f1, x, f2, x, f3)