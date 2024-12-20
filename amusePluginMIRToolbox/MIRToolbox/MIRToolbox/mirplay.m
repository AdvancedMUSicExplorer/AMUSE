function varargout = mirplay(a,varargin)
%   mirplay(a) plays audio signal, envelope, or pitches.
%       If a is an envelope, what is actually played is a white noise of
%           same envelope.
%       If a is a mirpitch object, pitches are played using sinusoids.
%   Optional arguments:
%       mirplay(...,'Channel',i) plays the channel(s) of rank(s) indicated by 
%           the array i.
%       mirplay(...,'Segment',k) plays the segment(s) of rank(s) indicated by 
%           the array k.
%       mirplay(...,'Sequence',l) plays the sequence(s) of rank(s) indicated
%           by the array l.
%       mirplay(...,'Increasing',d) plays the sequences in increasing order
%           of d, which could be either an array or a mirscalar data.
%       mirplay(...,'Decreasing',d) plays the sequences in decreasing order
%           of d, which could be either an array or a mirscalar data.
%       mirplay(...,'Every',s) plays every s sequence, where s is a number
%           indicating the step between sequences.
%       mirplay(...,'Burst',0) toggles off the burst sound between
%           segments.
%       Example: mirplay(mirenvelope('Folder'),...
%                        'increasing', mirrms('Folder'),...
%                        'every',5)


        ch.key = 'Channel';
        ch.type = 'Integer';
        ch.default = 0;
    option.ch = ch;

        sg.key = 'Segment';
        sg.type = 'Integer';
        sg.default = 0;
    option.sg = sg;

        se.key = 'Sequence';
        se.type = 'Integer';
        se.default = 0;
    option.se = se;

        inc.key = 'Increasing';
        inc.type = 'MIRtb';
    option.inc = inc;

        dec.key = 'Decreasing';
        dec.type = 'MIRtb';
    option.dec = dec;

        every.key = 'Every';
        every.type = 'Integer';
    option.every = every;

        burst.key = 'Burst';
        burst.type = 'Boolean';
        burst.default = 0; %1;
    option.burst = burst;

specif.option = option;

specif.eachchunk = 'Normal';

varargout = mirfunction(@mirplay,a,varargin,nargout,specif,@init,@main);


function [x type] = init(x,option)
type = '';


function noargout = main(a,option,postoption)
if iscell(a)
    a = a{1};
end
d = get(a,'Data');
if isa(a,'mirpitch')
    amp = get(a,'Amplitude');
end
f = get(a,'Sampling');
n = get(a,'Name');
c = get(a,'Channels');
fp = get(a,'FramePos');
t = get(a,'Title');
if not(option.se)
    if length(d)>1
        if isfield(option,'inc')
            [unused order] = sort(mirgetdata(option.inc));
        elseif isfield(option,'dec')
            [unused order] = sort(mirgetdata(option.dec),'descend');
        else
            order = 1:length(d);
        end
        if isfield(option,'every')
            order = order(1:option.every:end);
        end
    else
        order = 1;
    end
else
    order = option.se;
end
if not(isempty(order))
    for k = order(:)'
        if isamir(a,'miraudio')
            display(['Playing file: ' n{k}])
        else
            display(['Playing ',t,' related to file: ' n{k}])   
        end
        dk = d{k};
        if not(iscell(dk))
            dk = {dk};
        end
        if option.ch
            if isempty(c{k})
                chk = option.ch;
            else
                [unused unused chk] = intersect(option.ch,c{k});
            end
        else
            chk = 1:size(dk{1},3);
        end
        if isempty(chk)
            display('No channel to play.');
        end
        for l = chk
            if chk(end)>1
                display(['  Playing channel #' num2str(l)]);
            end
            if option.sg
                sgk = option.sg(find(option.sg<=length(dk)));
            else
                sgk = 1:length(dk);
            end
            for i = sgk
                if sgk(end)>1
                    display(['      Playing segment #' num2str(i)])
                end
                di = dk{i};
                if isa(a,'miraudio')
                    for j = 1:size(di,2)
                        tic
                        pl = audioplayer(di(:,j,l),f{k});
                        playblocking(pl);
                        % We should scale, in order to avoid clipping 
                        % (when reading AIF files for instance).
                        % But global scaling across segments, to avoid
                        % problem with silent segments.
                        idealtime = size(di,1)/f{k};
                        practime = toc;
                        if practime < idealtime
                            pause(idealtime-practime)
                        end
                    end
                else
                    synth = zeros(1,ceil((fp{k}{i}(end)-fp{k}{i}(1))*44100)+1);
                    if isa(a,'mirpitch')
                        stp = get(a,'Start');
                        transcribed = ~(isempty(stp) || isempty(stp{k}) ...
                                        || isempty(stp{k}{i}));
                    else
                        transcribed = 0;
                    end
                    if transcribed
                        stp = get(a,'Start');
                        enp = get(a,'End');
                        mep = get(a,'Mean');
                        stp = stp{k}{i}{1};
                        enp = enp{k}{i}{1};
                        mep = mep{k}{i}{1};
                        for j = 1:length(mep)
                            fj = mep(j); %2^(mep(j)/1200);
                            k1 = floor((fp{k}{i}(1,stp(j))-fp{k}{i}(1))*44100)+1;
                            k2 = floor((fp{k}{i}(2,enp(j))-fp{k}{i}(1))*44100)+1;
                            ampj = ones(1,k2-k1+1);
                            synth(k1:k2) = synth(k1:k2) ...
                                + sum(ampj.*sin(2*pi*fj*(0:k2-k1)/44100),1) ...
                                        .*hann(k2-k1+1)';
                        end
                    else
                        if isa(a,'mirpitch')
                            ampi = amp{k}{i};
                        end
                        for j = 1:size(di,2)
                            if iscell(di)
                                dj = di{j};
                            else
                                dj = di(:,j);
                            end
                            dj(isnan(dj)) = 0;
                            if isa(a,'mirpitch')
                                ampj = zeros(size(dj));
                                if iscell(ampi)
                                    ampj(1:size(ampi{j})) = ampi{j};
                                else
                                    ampj(1:size(ampi(:,j))) = ampi(:,j);
                                end
                            end
                            if not(isempty(dj))
                                k1 = floor((fp{k}{i}(1,j)-fp{k}{i}(1))*44100)+1;
                                k2 = floor((fp{k}{i}(2,j)-fp{k}{i}(1))*44100)+1;
                                if isa(a,'mirpitch')
                                    ampj = repmat(ampj,1,k2-k1+1);
                                else
                                    ampj = ones(size(dj),k2-k1+1);
                                end
                                synth(k1:k2) = synth(k1:k2) ...
                                    + sum(ampj.*sin(2*pi*dj*(0:k2-k1)/44100),1) ...
                                            .*hann(k2-k1+1)';
                            end
                        end
                    end
                    synth = synth/max(abs(synth));
                    pl = audioplayer(synth,44100);
                    playblocking(pl);
                end
                if option.burst && sgk(end)>1
                    pl = audioplayer(rand(1,10),8192);
                    playblocking(pl);
                end
            end
        end
    end
end
noargout = {};