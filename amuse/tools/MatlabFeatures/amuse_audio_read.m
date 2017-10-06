function [y, Fs] = amuse_audio_read( filename )
% y: Sampled data
% Fs: Samplerate in Hertz
    if verLessThan('matlab','8.4.0')
        [y, Fs] = wavread(filename);
    else
        [y, Fs] = audioread(filename);
    end
end

