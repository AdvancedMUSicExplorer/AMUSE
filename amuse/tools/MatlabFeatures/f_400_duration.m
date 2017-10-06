function res = f_400_duration( filename)
%
% computes duration in seconds of wav audio file
%
% siz: size of the audio data [number_of_samples number_of_channels]
    if verLessThan('matlab','8.4.0')
        % determine number of samples in the audio file
        res1 = wavread(filename,'size');

        % determine sampling rate and bit resolution of file
        % (but only reads the first sample of the file)
        [ytmp,fs,bits] = wavread(filename, 1);

        % duration = #samples / sampling rate
        res = res1(1) / fs;
    else
        info = audioinfo(filename);
        res = info.Duration;
    end
end