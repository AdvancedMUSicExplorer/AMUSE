function res = peak_positions(x)
%
% return array with same length as x where at the positions
% of maxima of x a one is returned and all other values are zero
%
% contact: Wolfgang.Theimer@nokia.com
% last modification: 25.04.2007
%

% extract length of array x
s = size(x);
N = s(1);

% determine maxima positions = location where sign of
% derivative changes from + to -
y = sign(x(2:N)-x(1:N-1));

res = [0 ; y(1:N-2)-y(2:N-1) ; 0] > 1;



