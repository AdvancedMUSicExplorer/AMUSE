function varargout = miraudio(orig,varargin)
%   a = miraudio('filename') loads the sound file 'filename' (in WAV or AU
%       format) into a miraudio object.
%   a = miraudio('Folder') loads all the sound files in the CURRENT folder
%       into a miraudio object.
%   a = miraudio(v,sr), where v is a column vector, translates the vector v
%       into a miraudio object. The sampling frequency is set to sr Hertz.
%           Default value for sr: 44100 Hz.
%   a = miraudio(b, ...), where b is already a miraudio object, performs 
%       operations on b specified by the optional arguments (see below).
%
%   Transformation options:
%       miraudio(...,'Mono',0) does not perform the default summing of
%           channels into one single mono track, but instead stores each 
%           channel of the initial soundfile separately.       
%       miraudio(...,'Center') centers the signals.
%       miraudio(...,'Sampling',r) resamples at sampling rate r (in Hz).
%           (Requires the Signal Processing Toolbox.)
%       miraudio(...,'Normal') normalizes with respect to RMS energy.
%   Extraction options:
%       miraudio(...,'Extract',t1,t2,u,f) extracts the signal between dates
%           t1 and t2, expressed in the unit u.
%           Possible values for u:
%               's' (seconds, by default),
%               'sp' (sample index, starting from 1).
%           The additional optional argument f indicates the referential
%               origin of the temporal positions. Possible values for f:
%               'Start' (by default)
%               'Middle' (of the sequence)
%               'End' of the sequence
%               When using 'Middle' or 'End', negative values for t1 or t2
%               indicate values before the middle or the end of the audio
%               sequence.
%       miraudio(...,'Trim') trims the pseudo-silence beginning and end off
%           the audio file. Silent frames are frames with RMS below t times
%           the medium RMS of the whole audio file.
%               Default value: t = 0.06
%           instead of 'Trim':
%              'TrimStart' only trims the beginning of the audio file,
%              'TrimEnd' only trims the end.
%           miraudio(...,'TrimThreshold',t) specifies the trimming threshold t.
%       miraudio(...,'Channel',c) or miraudio(...,'Channels',c) selects the
%           channels indicated by the (array of) integer(s) c.
%   Labeling option:
%       miraudio(...,'Label',l) labels the audio signal(s) following the 
%           label(s) l.
%           If l is a (series of) number(s), the audio signal(s) are
%           labelled using the substring of their respective file name of 
%           index l. If l=0, the audio signal(s) are labelled using the
%           whole file name.


if isempty(orig)
    varargout = {{}};
    return
end

if isnumeric(orig)
    if size(orig,2) > 1 || size(orig,3) > 1
        mirerror('MIRAUDIO','Only column vectors can be imported into mirtoolbox.');
    end
    if nargin == 1
        f = 44100;
    else
        f = varargin{1};
    end
    b = 32;
    if size(orig,1) == 1
        orig = orig';
    end
    tp = (0:size(orig,1)-1)'/f;
    l = (size(orig,1)-1); %/f;
    t = mirtemporal([],'Time',{{tp}},'Data',{{orig}},'Length',{{l}},...
                    'FramePos',{{tp([1 end])}},'Sampling',{f},...
                    'Name',{inputname(1)},'Label',{{}},'Clusters',{{}},...
                    'Channels',[],'Centered',0,'NBits',{b},...
                    'Title','Audio signal',...
                    'PeakPos',{{{}}},'PeakVal',{{{}}},'PeakMode',{{{}}});
    aa.fresh = 1;
    aa.extracted = 0;
    varargout = {class(aa,'miraudio',t)};
    return
end


        center.key = 'Center';
        center.type = 'Boolean';
        center.default = 0;
        center.when = 'After';
    option.center = center;
        
        normal.key = 'Normal';
        normal.type = 'String';
        normal.choice = {'RMS','Max'};
        normal.default = 0;
        normal.keydefault = 'RMS';
        normal.when = 'After';
    option.normal = normal;
    
        extract.key = {'Extract','Excerpt'};
        extract.type = 'Integer';
        extract.number = 2;
        extract.default = [];
        extract.unit = {'s','sp'};
        extract.defaultunit = 's';
        extract.from = {'Start','Middle','End'};
        extract.defaultfrom = 'Start';
    option.extract = extract;
        
        trim.type = 'String';
        trim.choice = {'NoTrim','Trim','TrimBegin','TrimStart','TrimEnd'};
        trim.default = 'NoTrim';
        trim.when = 'After';
    option.trim = trim;
        
        trimthreshold.key = 'TrimThreshold';
        trimthreshold.type = 'Integer';
        trimthreshold.default = .06;
        trimthreshold.when = 'After';
    option.trimthreshold = trimthreshold;
    
        smoothborder.key = 'SmoothBorder';
        smoothborder.type = 'Integer';
        smoothborder.default = 0;
        smoothborder.keydefault = 1;
        smoothborder.when = 'After';
    option.smoothborder = smoothborder;
        
        label.key = 'Label';
        label.default = '';
        label.when = 'After';
    option.label = label;
        
        sampling.key = 'Sampling';
        sampling.type = 'Integer';
        sampling.default = 0;
        sampling.when = 'Both';
    option.sampling = sampling;
        
   %     segment.key = 'Segment';
   %     segment.type = 'Integer';
   %     segment.default = [];
   %     segment.when = 'After';
   % option.segment = segment;

        mono.key = 'Mono';
        mono.type = 'Boolean';
        mono.default = NaN;
        mono.when = 'After';
    option.mono = mono; 
    
        fwr.key = 'FWR';
        fwr.type = 'Boolean';
        fwr.default = 0;
        fwr.when = 'After';
    option.fwr = fwr; 

        separate.key = 'SeparateChannels';
        separate.type = 'Boolean';
        separate.default = 0;
    option.separate = separate;    
    
        Ch.key = {'Channel','Channels'};
        Ch.type = 'Integer';
        Ch.default = [];
        Ch.when = 'After';
    option.Ch = Ch;
        
specif.option = option;

specif.beforechunk = {@beforechunk,'normal'};
specif.eachchunk = @eachchunk;
specif.combinechunk = @combinechunk;

if nargin > 1 && ischar(varargin{1}) && strcmp(varargin{1},'Now')
    if nargin > 2
        extract = varargin{2};
    else
        extract = [];
    end
    para = [];
    varargout = {main(orig,[],para,[],extract)};
else
    varargout = mirfunction(@miraudio,orig,varargin,nargout,specif,@init,@main);
end
if isempty(varargout)
    varargout = {{}};
end


function [x type] = init(x,option)
if isa(x,'mirdesign')
    if option.sampling
        x = setresampling(x,option.sampling);
    end
end
type = 'miraudio';


function a = main(orig,option,after,index,extract)
if iscell(orig)
    orig = orig{1};
end
if ischar(orig)
    if nargin < 5
        % When is this used?
        extract = [];
    else
        presil = extract(3);
        postsil = extract(4);
        extract = extract(1:2);
    end
    [d{1},tp{1},fp{1},f{1},l{1},b{1},n{1},ch{1}] = mirread(extract(1:2),orig,1,0);
    l{1}{1} = l{1}{1}*f{1};
    if presil
        d{1}{1} = [zeros(2000,1,size(d{1}{1},3));d{1}{1}];
        tp1 = tp{1}{1};
        tp{1}{1} = [tp1(1)-(2000:-1:1)'*(tp1(2)-tp1(1));tp1];
    end
    if postsil
        d{1}{1} = [d{1}{1};zeros(2000,1,size(d{1}{1},3))];
        tp1 = tp{1}{1};
        tp{1}{1} = [tp1;tp1(end)+(1:2000)'*(tp1(2)-tp1(1))];
    end
    t = mirtemporal([],'Time',tp,'Data',d,'FramePos',fp,'Sampling',f,...
                       'Name',n,'Label',cell(1,length(d)),...
                       'Clusters',cell(1,length(d)),'Length',l,...
                       'Channels',ch,'Centered',0,'NBits',b);
    t = set(t,'Title','Audio waveform');
    a.fresh = 1;
    a.extracted = 1;
    a = class(a,'miraudio',t);
else
    if not(isempty(option)) && not(isempty(option.extract))
        if not(isstruct(after))
            after = struct;
        end
        after.extract = option.extract;
    end
    if isa(orig,'miraudio')
        a = orig;
    else
        a.fresh = 1;
        a.extracted = 0;
        a = class(a,'miraudio',orig);
    end
end      
if not(isempty(after))
    a = post(a,after);
end    


function a = post(a,para)
if a.fresh && isfield(para,'mono')
    a.fresh = 0;
    if isnan(para.mono)
        para.mono = 1;
    end
end
if isfield(para,'mono') && para.mono == 1
    a = mirsum(a,'Mean');
end
d = get(a,'Data');
t = get(a,'Time');
ac = get(a,'AcrossChunks');
f = get(a,'Sampling');
cl = get(a,'Clusters');
for h = 1:length(d)
    for k = 1:length(d{h})
        tk = t{h}{k};
        dk = d{h}{k};
        if isfield(para,'extract') && not(isempty(para.extract)) ...
                && ~a.extracted
            t1 = para.extract(1);
            t2 = para.extract(2);
            if para.extract(4)
                if para.extract(4) == 1
                    shift = round(size(tk,1)/2);
                elseif para.extract(4) == 2
                    shift = size(tk,1);
                end
                if para.extract(3)
                    shift = tk(shift,1,1);
                end
                t1 = t1+shift;
                t2 = t2+shift;
            end                
            if para.extract(3) % in seconds
                ft = find(tk>=t1 & tk<=t2);
            else               % in samples
                if not(t1)
                    warning('WARNING IN MIRAUDIO: Extract sample positions should be real positive integers.')
                    display('Positions incremented by one.');
                    t1 = t1+1;
                    t2 = t2+1;
                end
                ft = t1:t2;
            end
            tk = tk(ft,:,:);
            dk = dk(ft,:,:);
        end
        if isfield(para,'Ch') && not(isempty(para.Ch))
            dk = dk(:,:,para.Ch);
        end
        if isfield(para,'center') && para.center
            dk = center(dk);
            a = set(a,'Centered',1);
        end
        if isfield(para,'normal') && not(isequal(para.normal,0))
            nl = size(dk,1);
            nf = size(dk,2);
            nc = size(dk,3);
            if isempty(ac)
                if strcmpi(para.normal,'RMS')
                    ee = 0;
                    for j = 1:nc
                        for i = 1:nf
                            ee = ee+sum(dk(:,i,j).^2);
                        end
                    end
                    ee = sqrt(ee/nl/nc/nf);
                elseif strcmpi(para.normal,'Max')
                    ee = max(max(max(abs(dk),[],1),[],2),[],3);
                else
                    mirerror('MIRAUDIO','Incorrect parameter for ''Normal'' option');
                end
            elseif strcmpi(para.normal,'RMS')
                ee = sqrt(sum(ac.sqrsum)/ac.samples);
            elseif strcmpi(para.normal,'Max')
                ee = ac.max;
            end
            if ee
                dk = dk./repmat(ee,[nl,nf,nc]);
            end
        end
        if isfield(para,'trim') && not(isequal(para.trim,0)) ... %%%% NOT A POST OPERATION!!
                && not(strcmpi(para.trim,'NoTrim'))
            if not(para.trimthreshold)
                para.trimthreshold = 0.06;
            end
            trimframe = 100;
            trimhop = 10;
            nframes = floor((length(tk)-trimframe)/trimhop)+1;
            rms = zeros(1,nframes);
            ss = sum(dk,3);
            for j = 1:nframes
                st = floor((j-1)*trimhop)+1;
                rms(j) = norm(ss(st:st+trimframe-1))/sqrt(trimframe);
            end
            rms = (rms-min(rms))./(max(rms)-min(rms));
            nosil = find(rms>para.trimthreshold);
            if strcmpi(para.trim,'Trim') || strcmpi(para.trim,'TrimStart') ...
                                         || strcmpi(para.trim,'TrimBegin')
                nosil1 = min(nosil);
                if nosil1 > 1
                    nosil1 = nosil1-1;
                end
                n1 = floor((nosil1-1)*trimhop)+1;
            else
                n1 = 1;
            end
            if strcmpi(para.trim,'Trim') || strcmpi(para.trim,'TrimEnd')
                nosil2 = max(nosil);
                if nosil2 < length(rms)
                    nosil2 = nosil2+1;
                end
                n2 = floor((nosil2-1)*trimhop)+1;
            else
                n2 = length(tk);
            end
            tk = tk(n1:n2);
            dk = dk(n1:n2,1,:);
        end
        if isfield(para,'smoothborder') && para.smoothborder
            [Lx,Ly,Lz] = size(dk);
            Lw = para.smoothborder / 1000 * f{k};
            w = ones(size(dk));
            l = min(floor(Lx/2),Lw);
            hw = hann(l*2);
            w(1:l,:,:) = repmat(hw(1:l),[1,Ly,Lz]);
            w(Lx-l+1:Lx,:,:) = repmat(flipud(hw(1:l)),[1,Ly,Lz]);
            dk = dk .* w;
        end
        if isfield(para,'sampling') && para.sampling
            if and(f{k}, not(f{k} == para.sampling))
                for j = 1:size(dk,3)
                    rk(:,:,j) = resample(dk(:,:,j),para.sampling,f{k});
                end
                dk = rk;
                tk = repmat((0:size(dk,1)-1)',[1 1 size(tk,3)])...
                            /para.sampling + tk(1,:,:);
            end
            f{k} = para.sampling;
        end
        if isfield(para,'fwr') && para.fwr
            dk = abs(dk);
        end
        d{h}{k} = dk;
        t{h}{k} = tk;
        %if isfield(para,'reverse') && para.reverse
        %    d{h}{k} = flipdim(d{h}{k},1);
        %end
    end
end
a = set(a,'Data',d,'Time',t,'Sampling',f,'Clusters',cl);
a = set(a,'Extracted',0);
if isfield(para,'label') 
    if isnumeric(para.label)
        n = get(a,'Name');
        l = cell(1,length(d));
        for k = 1:length(d)
            if para.label
                l{k} = n{k}(para.label);
            else
                l{k} = n{k};
            end
        end
        a = set(a,'Label',l);
    elseif iscell(para.label)
        idx = mod(get(a,'Index'),length(para.label));
        if not(idx)
            idx = length(para.label);
        end
        a = set(a,'Label',para.label{idx});
    elseif ischar(para.label) && ~isempty(para.label)
        l = cell(1,length(d));
        for k = 1:length(d)
            l{k} = para.label;
        end
        a = set(a,'Label',l);
    end
end


function [new orig] = beforechunk(orig,option,postoption)
option.normal = 0;
a = miraudio(orig,option);
d = get(a,'Data');
old = get(orig,'AcrossChunks');
if isempty(old)
    old.sqrsum = 0;
    old.samples = 0;
    old.max = 0;
end
new = mircompute(@crossum,d,postoption.mono);
new = new{1}{1};
new.sqrsum = old.sqrsum + new.sqrsum;
new.samples = old.samples + new.samples;
new.max = max(old.max,new.max);


function s = crossum(d,mono)
if isnan(mono) || mono
    d = mean(d,3);
end
s.sqrsum = sum(sum(sum(d.^2)));
s.samples = numel(d);
s.max = max(max(max(abs(d))));


function [y orig] = eachchunk(orig,option,missing)
y = miraudio(orig,option);


function y = combinechunk(old,new)
do = get(old,'Data');
to = get(old,'Time');
dn = get(new,'Data');
tn = get(new,'Time');
y = set(old,'Data',{{[do{1}{1};dn{1}{1}]}},...
            'Time',{{[to{1}{1};tn{1}{1}]}});