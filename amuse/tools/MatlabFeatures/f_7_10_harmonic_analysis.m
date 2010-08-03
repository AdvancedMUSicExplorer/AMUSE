%
% This file is part of AMUSE framework (Advanced MUsic Explorer).
%  
% Copyright 2006-2010 by code authors
% 
% Created at TU Dortmund, Chair of Algorithm Engineering
% (Contact: <http://ls11-www.cs.tu-dortmund.de>) 
% 
% AMUSE is free software: you can redistribute it and/or modify
% it under the terms of the GNU Lesser General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
% 
% AMUSE is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
% GNU Lesser General Public License for more details.
% 
% You should have received a copy of the GNU Lesser General Public License
% along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
%  
% Creation date: 17.12.2009
% Last modification: 07.06.2010
%
% Authors: Igor Vatolkin, Wolfgang Theimer
%
function res = f_7_10_harmonic_analysis(x,fs,wlength,woffset,ff)
%
% This function returns the 1st and 2nd tristimulus values and the
% normalized energy of harmonic components.
%
% x is the audio signal provided e.g. by [x,fs]=wavread('file.wav')
%
% fs is the sampling rate
%
% wlength is the length of analysis time window (default: 512)
%
% woffest is the offset of analysis time window (default: 512)
%
% ff is the vector with previously extracted fundamental frequencies for
% time windows as defined in wlength and woffset


% Consider only first channel of signal
x = x(:,1);

% Define Hamming window of wlength samples
w = hamming(wlength);

% Determine number of time windows
s = size(x);
nwin = floor(1 + (s(1)-wlength)/woffset);

% Frequency step, e.g. ~43 Hz if fs = 22050Hz and wlength = woffset = 512
freq_step = fs / wlength; 

% Vector with results of harmonic analysis:
% res(1,i): 1st tristimulus value for i-th window
% res(2,i): 2nd tristimulus value for i-th window
% res(3,i): normalized energy of harmonic components for i-th window. 
% It is equal to (1-noisiness)
% ATTENTION: the correctness of the estimated values depends strongly on
% the correctness of the given vector ff with fundamental frequencies!!!
res = zeros(3,nwin);

% Go through all time windows
for i = 1:nwin
   
   % If the fundamental frequency hasn't been estimated properly, go further
   if(isnan(ff(i)) || ff(i) == 0)
	res(1,i) = NaN;
	res(2,i) = NaN;
	res(3,i) = NaN;
	continue;
   end;

   % Perform Hamming windowing for window i
   y = x(1+(i-1)*woffset:wlength+(i-1)*woffset) .* w;

   % Compute magnitude of FFT for window i
   A = abs(fft(y,wlength));
   A = A(1:wlength/2);

   % Calculate sum of all FFT amplitudes
   A_sum = 0;
   for c = 1 : length(A)
       A_sum = A_sum + A(c);
   end;

   % Maximal possible number of harmonics in the FFT of the current time
   % window
   max_harmonics_index = floor((fs/2)/ff(i));
   
   % Harmonic amplitudes are saved here
   HA = zeros(1,max_harmonics_index);

   % Sum of all harmonic amplitudes
   HA_sum = 0;

   % Go through all harmonics
   for current_harmonics_index = 1 : max_harmonics_index-1

        % Frequency of the current harmonics	
        freq_of_current_harmonics = current_harmonics_index * ff(i);
	
        % Calculate the lower and upper approximations of harmonics in a given FFT vector
        l_fft_index = floor(freq_of_current_harmonics/freq_step);
        u_fft_index = ceil(freq_of_current_harmonics/freq_step);
        
        % If the both approximations are equal
        if(l_fft_index == u_fft_index) 
            HA(current_harmonics_index) = A(l_fft_index);
        
        % If the first harmonics is below the first available frequency
        % (default of the first frequency is 43.0664Hz)
        elseif(l_fft_index == 0)
            
            % Interpolate between 0 and amplitude of the 1st frequency bin for the approximation of harmonics amplitude
            HA(current_harmonics_index) = interp1([0 u_fft_index*freq_step]',[0 A(u_fft_index)]',freq_of_current_harmonics);
        else
            
            % Interpolate between both amplitudes for the approximation of harmonics amplitude
            HA(current_harmonics_index) = interp1([l_fft_index*freq_step u_fft_index*freq_step]',[A(l_fft_index) A(u_fft_index)]',freq_of_current_harmonics);
        end;
    
        % Update the sum of harmonic amplitudes	
        HA_sum = HA_sum + HA(current_harmonics_index);
   end

   % 1st tristimulus value
   if(max_harmonics_index > 0)   
        res(1,i) = HA(1) / HA_sum;
   else
        res(1,i) = NaN;
   end;

   % 2nd tristimulus value
   if(max_harmonics_index > 3)   
        res(2,i) = (HA(2) + HA(3) + HA(4)) / HA_sum;
   else
        res(2,i) = NaN;
   end;

   % Normalized energy of harmonic components
   res(3,i) = HA_sum / A_sum;
end


