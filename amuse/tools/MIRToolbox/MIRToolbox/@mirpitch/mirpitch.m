function varargout = mirpitch(orig,varargin)
%   p = mirpitch(x) evaluates the pitch frequencies (in Hz).
%   Specification of the method(s) for pitch estimation (these methods can
%       be combined):
%       mirpitch(...,'Autocor') computes an autocorrelation function
%           (Default method)
%           mirpitch(...'Enhanced',a) computes enhanced autocorrelation
%               (see help mirautocor)
%              toggled on by default
%           mirpitch(...,'Compress',k) performs magnitude compression
%               (see help mirautocor)
%           mirpitch(...,fb) specifies a type of filterbank.
%               Possible values:
%                   fb = 'NoFilterBank': no filterbank decomposition
%                   fb = '2Channels' (default value)
%                   fb = 'Gammatone' 
%       mirpitch(...,'Spectrum') computes the FFT spectrum
%       mirpitch(...,'AutocorSpectrum') computes the autocorrelation of
%           the FFT spectrum
%       mirpitch(...,'Cepstrum') computes the cepstrum
%       Alternatively, an autocorrelation or a cepstrum can be directly
%           given as first argument of the mirpitch function.
%   Peak picking options:
%       mirpitch(...,'Total',m) selects the m best pitches.
%           Default value: m = Inf, no limit is set concerning the number
%           of pitches to be detected.
%       mirpitch(...,'Mono') corresponds to mirpitch(...,'Total',1)
%       mirpitch(...,'Min',mi) indicates the lowest frequency taken into
%           consideration.
%           Default value: 75 Hz. (Praat)
%       mirpitch(...,'Max',ma) indicates the highest frequency taken into
%           consideration. 
%           Default value: 2400 Hz. Because there seems to be some problems
%           with higher frequency, due probably to the absence of 
%           pre-whitening in our implementation of Tolonen and Karjalainen
%           approach (used by default, cf. below).
%       mirpitch(...,'Contrast',thr) specifies a threshold value.
%           (see help peaks)
%           Default value: thr = .1
%       mirpitch(...,'Order',o) specifies the ordering for the peak picking.
%           Default value: o = 'Amplitude'.
%       Alternatively, the result of a mirpeaks computation can be directly
%           given as first argument of the mirpitch function.
%   Post-processing options:
%       mirpitch(..., 'Cent') convert the pitch axis from Hz to cent scale.
%           One octave corresponds to 1200 cents, so that 100 cents
%           correspond to a semitone in equal temperament.
%       mirpitch(..., 'Segment') segments the obtained monodic pitch curve
%           in cents as a succession of notes with stable frequencies.
%           Additional parameters available: 'SegMinLength', 'SegPitchGap',
%               'SegTimeGap'.
%       mirpitch(...,'Sum','no') does not sum back the channels at the end 
%           of the computation. The resulting pitch information remains
%           therefore decomposed into several channels.
%       mirpitch(...,'Median') performs a median filtering of the pitch
%           curve. When several pitches are extracted in each frame, the
%           pitch curve contains the best peak of each successive frame.
%       mirpitch(...,'Stable',th,n) remove pitch values when the difference 
%           (or more precisely absolute logarithmic quotient) with the
%           n precedent frames exceeds the threshold th. 
%           if th is not specified, the default value .1 is used
%           if n is not specified, the default value 3 is used
%       mirpitch(...'Reso',r) removes peaks whose distance to one or
%           several higher peaks is lower than a given threshold.
%           Possible value for the threshold r:
%               'SemiTone': ratio between the two peak positions equal to
%                   2^(1/12)
%       mirpitch(...,'Frame',l,h) orders a frame decomposition of window
%           length l (in seconds) and hop factor h, expressed relatively to
%           the window length. For instance h = 1 indicates no overlap.
%           Default values: l = 46.4 ms and h = 10 ms (Tolonen and
%           Karjalainen, 2000)
%   Preset model:
%       mirpitch(...,'Tolonen') implements (part of) the model proposed in
%           (Tolonen & Karjalainen, 2000). It is equivalent to
%           mirpitch(...,'Enhanced',2:10,'Generalized',.67,'2Channels')
%   [p,a] = mirpitch(...) also displays the result of the method chosen for
%       pitch estimation, and shows in particular the peaks corresponding
%       to the pitch values.
%   p = mirpitch(f,a,<r>) creates a mirpitch object based on the frequencies
%       specified in f and the related amplitudes specified in a, using a
%       frame sampling rate of r Hz (set by default to 100 Hz).
%
%   T. Tolonen, M. Karjalainen, "A Computationally Efficient Multipitch 
%       Analysis Model", IEEE TRANSACTIONS ON SPEECH AND AUDIO PROCESSING,
%       VOL. 8, NO. 6, NOVEMBER 2000

        ac.key = 'Autocor';
        ac.type = 'Boolean';
        ac.default = 0;
    option.ac = ac;
    
            enh.key = 'Enhanced';
            enh.type = 'Integer';
            enh.default = 2:10;
        option.enh = enh;

            filtertype.type = 'String';
            filtertype.choice = {'NoFilterBank','2Channels','Gammatone'};
            filtertype.default = '2Channels';
        option.filtertype = filtertype;

            sum.key = 'Sum';
            sum.type = 'Boolean';
            sum.default = 1;
        option.sum = sum;

            gener.key = {'Generalized','Compress'};
            gener.type = 'Integer';
            gener.default = .5;
        option.gener = gener;

        as.key = 'AutocorSpectrum';
        as.type = 'Boolean';
        as.default = 0;
    option.as = as;
    
        s.key = 'Spectrum';
        s.type = 'Boolean';
        s.default = 0;
    option.s = s;

            res.key = 'Res';
            res.type = 'Integer';
            res.default = NaN;
        option.res = res;
  
            db.key = 'dB';
            db.type = 'Integer';
            db.default = 0;
            db.keydefault = Inf;
        option.db = db;
    
            norm.key = 'Normal';
            norm.type = 'Boolean';
            norm.default = 0;
        option.norm = norm;

            terhardt.key = 'Terhardt';
            terhardt.type = 'Boolean';
            terhardt.default = 0;
        option.terhardt = terhardt;
        
        ce.key = 'Cepstrum';
        ce.type = 'Boolean';
        ce.default = 0;
    option.ce = ce;

        comb.key = 'Comb'; %%%%% NEW
        comb.type = 'Boolean';
        comb.default = 0;
    option.comb = comb;
    
%% peak picking options

        m.key = 'Total';
        m.type = 'Integer';
        m.default = Inf;
    option.m = m;
    
        multi.key = 'Multi';
        multi.type = 'Boolean';
        multi.default = 0;
    option.multi = multi;

        mono.key = 'Mono';
        mono.type = 'Boolean';
        mono.default = 0;
    option.mono = mono;

        mi.key = 'Min';
        mi.type = 'Integer';
        mi.default = 75;
    option.mi = mi;
        
        ma.key = 'Max';
        ma.type = 'Integer';
        ma.default = 2400;
    option.ma = ma;
        
        cthr.key = 'Contrast';
        cthr.type = 'Integer';
        cthr.default = .1;
    option.cthr = cthr;

        thr.key = 'Threshold';
        thr.type = 'Integer';
        thr.default = .4; % Why not .1?
    option.thr = thr;

        order.key = 'Order';
        order.type = 'String';
        order.choice = {'Amplitude','Abscissa'};
        order.default = 'Amplitude';
    option.order = order;    

        reso.key = 'Reso';
        reso.type = 'String';
        reso.choice = {0,'SemiTone'};
        reso.default = 0;
    option.reso = reso;
        
        normal.key = 'Normalize';
        normal.type = 'String';
        normal.choice = {'Local','Global','No'};
        normal.default = 'Global';
    option.normal = normal;
    
        localfactor.key = 'LocalFactor';
        localfactor.type = 'Integer';
        localfactor.default = 0;
        localfactor.keydefault = .99;
    option.localfactor = localfactor;
    
        harmonic.key = 'Harmonic';
        harmonic.type = 'Boolean';
        harmonic.default = 0;
    option.harmonic = harmonic;
    
        maxpeak.key = 'MaxPeak';   %%%%% NEW
        maxpeak.type = 'Integer';
        maxpeak.default = inf;
    option.maxpeak = maxpeak;
    
%% post-processing options
        
        cent.key = 'Cent';
        cent.type = 'Boolean';
        cent.default = 0;
    option.cent = cent;
    
        segm.key = 'Segment';
        segm.type = 'String';
        segm.choice = {'Lartillot','Nymoen','Lartillot2'};
        segm.when = 'Both';
        segm.default = '';
        segm.keydefault = 'Lartillot';
    option.segm = segm;

            segmin.key = 'SegMinLength';
            segmin.type = 'Integer';
            segmin.when = 'Both';
            segmin.default = 2;
        option.segmin = segmin;
        
            segpitch.key = 'SegPitchGap';
            segpitch.type = 'Integer';
            segpitch.when = 'Both';
            segpitch.default = 45;
        option.segpitch = segpitch;        

            segtime.key = 'SegTimeGap';
            segtime.type = 'Integer';
            segtime.when = 'Both';
            segtime.default = 20;
        option.segtime = segtime;      
        
            octgap.key = 'OctaveGap';
            octgap.type = 'Boolean';
            octgap.when = 'Both';
            octgap.default = 0;
        option.octgap = octgap;

        ref.key = 'Ref';
        ref.type = 'Integer';
        ref.default = 0;
    option.ref = ref;

        stable.key = 'Stable';
        stable.type = 'Integer';
        stable.number = 2;
        stable.default = [Inf 0];
        stable.keydefault = [.1 3];
    option.stable = stable;
    
        median.key = 'Median';
        median.type = 'Integer';
        median.default = 0;
        median.keydefault = .1;
    option.median = median;
    
        track.key = 'Track';  %%%%% NEW
        track.type = 'Boolean';
        track.default = 0;
    option.track = track;

        harmonics.key = 'Harmonics';  %%%%% NEW
        harmonics.type = 'Boolean';
        harmonics.default = 0;
    option.harmonics = harmonics;

        frame.key = 'Frame';
        frame.type = 'Integer';
        frame.number = 2;
        frame.default = [0 0];
        frame.keydefault = [NaN NaN];
    option.frame = frame;
    
        nooctave.key = 'NoOctave';  %%%%% NEW
        nooctave.type = 'Boolean';
        nooctave.default = 0;
    option.nooctave = nooctave; 
   
%% preset model

        tolo.key = 'Tolonen';
        tolo.type = 'Boolean';
        tolo.default = 0;
    option.tolo = tolo;
    
specif.option = option;
specif.chunkframebefore = 1;

if isnumeric(orig)
    if nargin<3
        f = 100;
    else
        f = varargin{2};
    end
    fp = (0:size(orig,1)-1)/f;
    fp = [fp;fp+1/f];
    p.amplitude = {{varargin{1}'}};
    s = mirscalar([],'Data',{{orig'}},'Title','Pitch','Unit','Hz',...
                     'FramePos',{{fp}},'Sampling',f,'Name',{inputname(1)});
    p = class(p,'mirpitch',s);
    varargout = {p};
else
    varargout = mirfunction(@mirpitch,orig,varargin,nargout,specif,@init,@main);
end



function [y type] = init(orig,option)
if option.tolo
    option.enh = 2:10;
    option.gener = .67;
    option.filtertype = '2Channels';
elseif option.harmonic
    option.s = 1;
    option.frame.hop.val = .1;
    option.res = 1;
    option.db = 60;   %%% Used to be Inf
end
if not(option.ac) && not(option.as) && not(option.ce) && not(option.s)
    option.ac = 1;
end
if ~isempty(option.segm) && option.frame.length.val==0
    option.frame.length.val = NaN;
    option.frame.hop.val = NaN;
end
if isnan(option.frame.length.val)
    if option.comb
        option.frame.length.val = .2;
    else
        option.frame.length.val = .0464;
    end
end
if isnan(option.frame.hop.val)
    option.frame.hop.val = .01;
    option.frame.hop.unit = 's';
end
if isamir(orig,'mirmidi') || isamir(orig,'mirscalar') || haspeaks(orig)
    y = orig;
else
    if isamir(orig,'mirautocor')
        y = mirautocor(orig,'Min',option.mi,'Hz','Max',option.ma,'Hz','Freq');
    elseif isamir(orig,'mircepstrum')
        y = orig;
    elseif isamir(orig,'mirspectrum')
        if not(option.as) && not(option.ce) && not(option.s)
            option.ce = 1;
        end
        if option.as
            y = mirautocor(orig,...
                            'Min',option.mi,'Hz','Max',option.ma,'Hz');
        end
        if option.ce
            ce = mircepstrum(orig,'freq',...
                            'Min',option.mi,'Hz','Max',option.ma,'Hz');
            if option.as
                y = y*ce;
            else
                y = ce;
            end
        end
        if option.s
            y = orig;
        end
    else
        if option.ac
            x = orig;
            if not(strcmpi(option.filtertype,'NoFilterBank'))
                x = mirfilterbank(x,option.filtertype);
            end
            x = mirframenow(x,option);
            y = mirautocor(x,'Generalized',option.gener);%,...
                               % 'Min',option.mi,'Hz','Max',option.ma,'Hz');
            if option.sum
                y = mirsummary(y);
            end
            y = mirautocor(y,'Enhanced',option.enh,'Freq');
            y = mirautocor(y,'Min',option.mi,'Hz','Max',option.ma,'Hz');
        end
        if option.as || option.ce || option.s
            x = mirframenow(orig,option);
            if option.comb
                y = mirspectrum(x,'Min',option.mi,'Max',2500);
            elseif option.s
                s = mirspectrum(x,'Min',option.mi,'Max',option.ma,...
                                  'Res',option.res,'dB',option.db,...
                                  'Normal',option.norm,...
                                  'Terhardt',option.terhardt);
                if option.ac
                    y = y*s;
                else
                    y = s;
                end
            end
            if option.as || option.ce
                s = mirspectrum(x);
                if option.as
                    as = mirautocor(s,'Min',option.mi,'Hz',...
                                      'Max',option.ma,'Hz');
                    if option.ac || option.s
                        y = y*as;
                    else
                        y = as;
                    end
                end
                if option.ce
                    ce = mircepstrum(s,'freq','Min',option.mi,'Hz',...
                                              'Max',option.ma,'Hz');
                    if option.ac || option.s || option.as
                        y = y*ce;
                    else
                        y = ce;
                    end
                end
            end
        end
    end
end
type = {'mirpitch',mirtype(y)};
    

function o = main(x,option,postoption)
if option.comb == 2
    option.m = Inf;
    option.order = 'Abscissa';
elseif option.multi && option.m == 1
    option.m = Inf;
elseif (option.mono && option.m == Inf) %|| option.segm
    option.m = 1;
elseif option.harmonic
    option.cthr = .01;
    option.thr = .05;   %%% Used to be .5
end
if iscell(x)
    if length(x)>1
        x2 = get(x{2},'Data');
        f2 = get(x{2},'Pos');
    end
    x = x{1};
else
    x2 = [];
end

if option.comb == 1
    d = get(x,'Data');
    pos = get(x,'Pos');
    cb = cell(1,length(d));
    for i = 1:length(d)
        cb{i} = cell(1,length(d{i}));
        for j = 1:length(d{i})
            cb{i}{j} = zeros(size(d{i}{j},1),...
                             size(d{i}{j},2),...
                             size(d{i}{j},3));
            dij = d{i}{j}/max(max(max(d{i}{j})));
            for h = 1:size(d{i}{j},1)
                ph = pos{i}{j}(h,1,1);
                ip = h;
                for k = 2:size(d{i}{j},1)
                    [unused mp] = min(abs(pos{i}{j}(ip(end)+1:end,1,1) ...
                                          - ph * k));
                    if isempty(mp)
                        break
                    end
                    ip(end+1) = ip(end) + mp;
                end
                if length(ip) == 1
                    break
                end
                cbh = sum(dij(ip,:,:));
                for k = 1:length(ip)
                    cbh = cbh .* ...
                        (.5 * (2 - ...
                               exp(-(max(dij(ip(1:k),:,:),[],1).^2 * 5000))));
                end
                cb{i}{j}(h,:,:) = cbh;
            end
            cb{i}{j}(h+1:end,:,:) = [];
            pos{i}{j}(h+1:end,:,:) = [];
        end
    end
    x = set(x,'Data',cb,'Pos',pos,'Title','Spectral Comb');
end

if isa(x,'mirpitch')
    pf = get(x,'Data');
    pa = get(x,'Amplitude');
    if option.m < Inf
        for i = 1:length(pf)
            for j = 1:length(pf{i})
                for h = 1:length(pf{i}{j})
                    pf{i}{j}{h} = pf{i}{j}{h}(1:option.m,:);
                    pa{i}{j}{h} = pa{i}{j}{h}(1:option.m,:);
                end
            end
        end
    end
else
    if 0 %option.harmonic
        option.normal = 'Local';
        option.localfactor = .99;
    end
    if not(isa(x,'mirpitch') || isa(x,'mirmidi'))
        x = mirpeaks(x,'Total',option.m,...,
                       'Contrast',option.cthr,'Threshold',option.thr,...
                       'Reso',option.reso,'NoBegin','NoEnd',...
                       'Order',option.order,...
                       'Harmonic',option.harmonic,'MaxPeak',option.maxpeak,...
                       'Normalize',option.normal,...
                       'LocalFactor',option.localfactor);
    end
    if isa(x,'mirscalar')
        pf = get(x,'Data');
    elseif option.harmonic
        pf = get(x,'TrackPos');
        pa = get(x,'TrackVal');
    else
        pf = get(x,'PeakPrecisePos');
        pa = get(x,'PeakPreciseVal');
    end
end

fp = get(x,'FramePos');

punit = 'Hz';

if option.comb == 2
    pp = get(x,'PeakPos');
    pv = get(x,'PeakVal');
    pm = get(x,'PeakMode');
    f = get(x,'Pos');
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            maxf = f{i}{j}(end,1);
            tracks = {};
            melody.time = [];
            melody.f = [];
            melody.track = 0;
            counter.time = [];
            counter.f = [];
            counter.track = 0;
            for h = 1:length(pf{i}{j})
                found = zeros(1,length(tracks));
                sco = zeros(length(pf{i}{j}{h}),1);
                for k = 1:length(pf{i}{j}{h})
                    fk = pf{i}{j}{h}(k);
                    if fk < option.mi || fk > option.ma
                        break
                    end
                    ws{k} = zeros(round(maxf / fk) ,1);
                    %err = mod(pf{i}{j}{h}/fk,1);
                    %err = min(err,1-err);
                    ws{k}(1) = pa{i}{j}{h}(k);
                    for l = k+1:length(pf{i}{j}{h})
                        r = round(pf{i}{j}{h}(l) / fk);
                        if r == 1
                            continue
                        end
                        pl = pa{i}{j}{h}(l);
%                         if pl > ws(1)
%                             continue
%                         end
                        err = mod(pf{i}{j}{h}(l) / fk ,1);
                        err = min(err,1-err);
                        ws{k}(r) = max(ws{k}(r),pl*.5*exp(-err^2*50));
                    end
                    
                    
                    l = 1;
                    stop = 0;
                    while l <= length(tracks)
                        d = tracks{l}.f(end) / fk;
                        if round(d) > 1 && abs(d-round(d)) < .15 %05
                            tracks(l) = [];
                            found(l) = [];
                        else
                            d = fk / tracks{l}.f(end);
                            if round(d) > 1 && abs(d-round(d)) < .15 %.05
                                stop = true;
                                break
                            end
                            l = l+1;
                        end
                    end
                    if stop
                        continue
                    end
                    
                    if isempty(tracks)
                        tracks{1}.index = h;
                        tracks{1}.f = fk;
                        tracks{1}.m = ws{k};
                        tracks{1}.k = k;
                        tracks{1}.currentm = sum(ws{k});
                    else
                        d = zeros(length(tracks),1);
                        for l = 1:length(tracks)
                            d(l) = abs(log2(tracks{l}.f(end) / fk));
                        end
                        [mv,mi] = min(d);
                        if mv > 1/12
                            tracks{end+1}.index = h;
                            tracks{end}.f = fk;
                            tracks{end}.m = ws{k};
                            tracks{end}.k = k;
                            tracks{end}.currentm = sum(ws{k});
                            found(end+1) = 1;
                        else
                            if found(mi) == 1
                                continue
                            end
                            tracks{mi}.index(end+1) = h;
                            tracks{mi}.f(end+1) = fk;
                            tracks{mi}.m(end+1:length(ws{k}),:) = 0;
                            tracks{mi}.m(1:length(ws{k}),end+1) = ws{k};
                            tracks{mi}.k = k;
                            tracks{mi}.currentm = sum(ws{k});
                            found(mi) = 1;
                        end
                        sco(k) = sum(ws{k});
                    end
                end
%                 h
%                 for ii = 1:length(tracks),tracks{ii}.f(end),end
                maxvalue = 0;
                for k = 1:length(tracks)
                    if tracks{k}.index(end) == h && tracks{k}.currentm > maxvalue
                        maxvalue = tracks{k}.currentm;
                    end
                end
                min_f = Inf;
                max_f = 0;
                max_k = [];
                for k = 1:length(tracks)
                    if tracks{k}.index(end) == h && tracks{k}.currentm > maxvalue * .7
                        if tracks{k}.f(end) < min_f
                            min_f = tracks{k}.f(end);
                            min_k = tracks{k}.k;
                        end
                        if tracks{k}.f(end) > max_f
                            max_f = tracks{k}.f(end);
                            max_k = tracks{k}.k;
                        end
                    end
                end
                if max_f && min_f ~= max_f
                    counter.time(end+1) = h;
                    counter.f(end+1) = min_f;
                    max_k = [min_k,max_k];
                end
                melody.time(end+1) = h;
                melody.f(end+1) = max_f;
                pf{i}{j}{h} = pf{i}{j}{h}(max_k);
                pa{i}{j}{h} = pa{i}{j}{h}(max_k);
                pp{i}{j}{h} = pp{i}{j}{h}(max_k);
                pv{i}{j}{h} = pv{i}{j}{h}(max_k);
                pm{i}{j}{h} = pm{i}{j}{h}(max_k);
            end
        end
    end
    x = set(x,'PeakPrecisePos',pf,'PeakPreciseVal',pa,...
              'PeakPos',pp,'PeakVal',pv,'PeakMode',pm);
elseif option.comb == 3
    pp = get(x,'PeakPos');
    pv = get(x,'PeakVal');
    pm = get(x,'PeakMode');
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            oldf = [];
            oldm = [];
            for h = 1:length(pf{i}{j})
                fh = pf{i}{j}{h};
                [fh,order] = sort(fh);
                mh = pa{i}{j}{h}(order);
                dmh = mh;
                for k = 1:length(fh)
                    fk = fh(k);
                    if fk > option.ma
                        fh(k:end) = [];
                        break
                    end
                    for l = 1:length(oldf)
                        d = abs(log2(oldf / fk));
                        if d < 1/12
                            dmh(k) = min(dmk(k),max(0,mh(k)-oldm(l)));
                        end
                    end
                end
                for k = 1:length(fh)
                    fk = fh(k);
                    mk = mh(k);
                    for l = k+1:length(fh)
                        d = fh(l) / fk;
                        if round(d) > 1 && abs(d-round(d)) < .15 %05
%                             mh(k) = mh(k) + mh(l);
                            mk = min(mk,mh(l));
                            mh(k) = mh(k) + mk;
                            dmh(k) = dmh(k) + dmh(l);
                        end
                    end
                end
                [unused,best] = max(mh);
                pf{i}{j}{h} = pf{i}{j}{h}(order(best));
                pa{i}{j}{h} = pa{i}{j}{h}(order(best));
                pp{i}{j}{h} = pp{i}{j}{h}(order(best));
                pv{i}{j}{h} = pv{i}{j}{h}(order(best));
                pm{i}{j}{h} = pm{i}{j}{h}(order(best));
                oldf = fh;
                oldm = mh;
            end
        end
    end
    x = set(x,'PeakPrecisePos',pf,'PeakPreciseVal',pa,...
              'PeakPos',pp,'PeakVal',pv,'PeakMode',pm);
elseif option.comb == 4
    pp = get(x,'PeakPos');
    pv = get(x,'PeakVal');
    pm = get(x,'PeakMode');
    f = get(x,'Pos');
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            tracks = {};
            melody.time = [];
            melody.f = [];
            melody.track = 0;
            counter.time = [];
            counter.f = [];
            counter.track = 0;
            for h = 1:length(pf{i}{j})
                found = zeros(1,length(tracks));
                for k = 1:length(pf{i}{j}{h})
                    fk = pf{i}{j}{h}(k);
                    if fk < option.mi || fk > option.ma
                        break
                    end
                    if isempty(tracks)
                        tracks{1}.index = h;
                        tracks{1}.f = fk;
                        tracks{1}.m = pa{i}{j}{h}(k);
                        tracks{1}.k = k;
                    else
                        d = zeros(length(tracks),1);
                        for l = 1:length(tracks)
                            d(l) = abs(log2(tracks{l}.f(end) / fk));
                        end
                        [mv,mi] = min(d);
                        if mv > 1/12
                            tracks{end+1}.index = h;
                            tracks{end}.f = fk;
                            tracks{end}.m = pa{i}{j}{h}(k);
                            tracks{end}.k = k;
                            found(end+1) = 1;
                        else
                            if found(mi) == 1
                                continue
                            end
                            tracks{mi}.index(end+1) = h;
                            tracks{mi}.f(end+1) = fk;
                            tracks{mi}.m(end+1) = pa{i}{j}{h}(k);
                            tracks{mi}.k = k;
                            found(mi) = 1;
                        end
                    end
                end
%                 h
%                 for ii = 1:length(tracks),tracks{ii}.f(end),end
                
                indices = [];
                for k = 1:length(tracks)
                    if tracks{k}.index(end) == h
                        indices(end+1) = tracks{k}.k;
                    end
                end

                pf{i}{j}{h} = pf{i}{j}{h}(indices);
                pa{i}{j}{h} = pa{i}{j}{h}(indices);
                pp{i}{j}{h} = pp{i}{j}{h}(indices);
                pv{i}{j}{h} = pv{i}{j}{h}(indices);
                pm{i}{j}{h} = pm{i}{j}{h}(indices);
            end
        end
    end
    x = set(x,'PeakPrecisePos',pf,'PeakPreciseVal',pa,...
              'PeakPos',pp,'PeakVal',pv,'PeakMode',pm);
elseif option.track
    pp = get(x,'PeakPos');
    pv = get(x,'PeakVal');
    pm = get(x,'PeakMode');
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            partials = {};
            notes = {};
            for h = 1:length(pf{i}{j})
%                 h,pf{i}{j}{h}
                k = 1;
                while k <= length(pf{i}{j}{h})
                    fk = pf{i}{j}{h}(k);
                    if fk < option.mi || fk > option.ma
                        pf{i}{j}{h}(k) = [];
                        pa{i}{j}{h}(k) = [];
                        pp{i}{j}{h}(k) = [];
                        pv{i}{j}{h}(k) = [];
                        pm{i}{j}{h}(k) = [];
                        continue
                    end
                    if isempty(partials)
                        partials{1}.index = h;
                        partials{1}.f = fk;
                        partials{1}.m = pa{i}{j}{h}(k);
                        partials{1}.k = k;
                        partials{1}.recorded = [];
                    else
                        d = zeros(length(partials),1);
                        for l = 1:length(partials)
                            if length(partials{l}.f) > 1 && ...
                                    partials{l}.index(end) == h
                                fend = partials{l}.f(end-1);
                            else
                                fend = partials{l}.f(end);
                            end
                            d(l) = abs(log2(fend / fk));
                        end
                        [mv,mi] = min(d);
                        if mv > 1/24
                            partials{end+1}.index = h;
                            partials{end}.f = fk;
                            partials{end}.m = pa{i}{j}{h}(k);
                            partials{end}.k = k;
                            partials{end}.recorded = [];
                        else
                            if partials{mi}.index(end) < h
                                partials{mi}.index(end+1) = h;
                                partials{mi}.f(end+1) = fk;
                                partials{mi}.m(end+1) = pa{i}{j}{h}(k);
                                partials{mi}.k = k;
                            elseif partials{mi}.m(end) < pa{i}{j}{h}(k)
                                partials{mi}.f(end) = fk;
                                partials{mi}.m(end) = pa{i}{j}{h}(k);
                                partials{mi}.k = k;
                            end
                        end
                    end
                    k = k+1;
                end
                
%                 h
%                 for ii = 1:length(partials),partials{ii}.f(end),end

                for k = 1:length(partials)
                    if partials{k}.index(end) == h
                        found = 0;
                        for l = 1:length(notes)
                            d = partials{k}.f(end) / notes{l}.f0;
                            rd = round(d);
                            if rd && abs(d-rd) < .03
                                notes{l}.f(rd,h) = partials{k}.f(end);
                                notes{l}.m(rd,h) = partials{k}.m(end);
                                notes{l}.h = h;
                                if rd == 1
                                    notes{l}.k = partials{k}.k;
                                end
                                found = 1;
                            end
                            
                            if ~found
                                d = notes{l}.f0 / partials{k}.f(end);
                                rd = round(d);
                                if rd > 1 && abs(d-rd) < .03
                                    newf = zeros(size(notes{l}.f,1)*rd,h);
                                    newm = zeros(size(notes{l}.f,1)*rd,h);
                                    newf(rd:rd:end,1:length(notes{l}.f)) = notes{l}.f;
                                    newm(rd:rd:end,1:length(notes{l}.m)) = notes{l}.m;
                                    notes{l}.f = newf;
                                    notes{l}.m = newm;
                                    notes{l}.f(1,h) = partials{k}.f(end);
                                    notes{l}.m(1,h) = partials{k}.m(end);
                                    notes{l}.f0 = partials{k}.f(end);
                                    notes{l}.k = partials{k}.k;
                                    notes{l}.h = h;
                                    found = 1;
                                    break
                                end
                            end
                        end
                        if ~found
                            notes{end+1}.f(1,h) = partials{k}.f(end);
                            notes{end}.m(1,h) = partials{k}.m(end);
                            notes{end}.f0 = partials{k}.f(end);
                            notes{end}.k = partials{k}.k;
                            notes{end}.h = h;
                        end
                    end
                end
                
                maxm = 0;
                for l = 1:length(notes)
                    if notes{l}.h == h
                        mend = notes{l}.m(:,end);
                        for g = 1:2:length(mend)
                            if ~mend(g)
                                mend(g:end) = [];
                                break
                            end
                        end
                        ml = max(mend);
%                         ml = mean(notes{l}.m(:,end)); %max(notes{l}.m(:,end)); %notes{l}.m(1,end); %sum(notes{l}.m(:,end));
                        if ml > maxm
                            maxm = ml;
                            indemax = l;
                        end
                    end
                end
                if maxm
                    indices = notes{indemax}.k;
                else
                    indices = [];
                end
                
%                  indices = [];
%                 currenti = [];
%                 currentm = [];
%                 for k = 1:length(partials)
%                     if partials{k}.index(end) == h
% %                         indices(end+1) = partials{k}.k;
%                         currenti(end+1) = k;
%                         currentm(end+1) = partials{k}.m(end);
%                     end
%                 end
%                 if isempty(currenti)
%                     main = [];
%                 elseif (isempty(main) || partials{main}.index(end) < h)
%                     if ~isempty(main) &&  length(partials{main}.recorded) < 10
%                         for k = 1:length(partials{main}.recorded)
%                             hh = partials{main}.recorded(k);
%                             pf{i}{j}{hh} = [];
%                             pa{i}{j}{hh} = [];
%                             pp{i}{j}{hh} = [];
%                             pv{i}{j}{hh} = [];
%                             pm{i}{j}{hh} = [];
%                         end
%                     end
%                     
%                     [unused,ma] = max(currentm);
%                     main = currenti(ma);
%                 end
%                 if isempty(main)
%                     indices = [];
%                 else
%                     indices = partials{main}.k;
%                     partials{main}.recorded(end+1) = h;
%                 end

%                 [unused,ma] = max(pa{i}{j}{h});
%                 indices = ma;
                
                pf{i}{j}{h} = pf{i}{j}{h}(indices);
                pa{i}{j}{h} = pa{i}{j}{h}(indices);
                pp{i}{j}{h} = pp{i}{j}{h}(indices);
                pv{i}{j}{h} = pv{i}{j}{h}(indices);
                pm{i}{j}{h} = pm{i}{j}{h}(indices);
            end
        end
    end
    x = set(x,'PeakPrecisePos',pf,'PeakPreciseVal',pa,...
              'PeakPos',pp,'PeakVal',pv,'PeakMode',pm);
%%
elseif option.harmonics % NEW VERSION
%     dx = mirspectrum(x,'Diff',5);
%     dx = mirpeaks(dx,'Total',option.m,...,'Track',option.track,...
%                        'Contrast',option.cthr,'Threshold',option.thr,...
%                        'Reso',option.reso,'NoBegin','NoEnd',...
%                        'Order',option.order,'Harmonic',option.harmonic,...
%                       ... 'Highest',option.highest,...
%                        'Normalize',option.normal,...
%                        'LocalFactor',option.localfactor);
    pp = get(x,'PeakPrecisePos');
    pv = get(x,'PeakPreciseVal');
%     dpp = get(dx,'PeakPrecisePos');
%     dpv = get(dx,'PeakPreciseVal');
    f = get(x,'Pos');
    dd = get(x,'Data');
    beta = 0; %.001; %.005;

    
%     ratio = 2^.08; %(1/18); %2^(1/24);
%     tolratio = .08;
    ratio = 2^.3; %(1/18); %2^(1/24);
    tolratio = .3;

    
    
    nharm = 15;
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            fj = f{i}{j};
            difffj = diff(f{i}{j}(1:2,1));
            if0 = f{i}{j}(1) / difffj;
            dij = dd{i}{j};
            dij0 = [zeros(if0-1,size(dij,2));dij];
            f0s = struct('meanf0',{},'globmg',{},'mgs',{},'sub',{},'super',{},'partials',{});
            active = [];
%             figure, hold on
            for h = 1:length(pf{i}{j})
                mh = max(dij(:,h));
                if ~mod(h,1000)
                    h/length(pf{i}{j})
                end
%                 h,pf{i}{j}{h}
                ph = pp{i}{j}{h}';
                pvh = pv{i}{j}{h};
%                 dph = dpp{i}{j}{h}';
%                 dpvh = dpv{i}{j}{h};
                av = ones(1,length(ph));
                dh = interp1(1:size(dij0,1),dij0(:,h),.1:.1:size(dij0,1));
%                 plot(log10(dh))
                k = 1;
                acts = length(active);
                while k <= acts
                    a = active(k);
                    
                    if length(f0s(a).meanf0) == h
                        k = k + 1;
                        continue
                    end
                    
                    if 0 %%%%%%%dh(round(f0s(a).meanf0(end) / difffj* 10)) < .2
                        active(k) = [];
                        acts = acts - 1;
                        continue
                    end
                    
                    delta = (-3:.1:3)';
                    ifk = (f0s(a).meanf0(end) + delta) / difffj * (1:nharm);
                    ifk(ifk*10 > length(dh)) = 1;
                    ifk3 = ifk(:,3:end);
                    
                    dhs = dh(round(ifk3*10));
                    if size(dhs,1) == 1
                        dhs = dhs';
                    end
                    score = sum(dhs.*repmat(1:(nharm-2),[length(delta),1]),2);
                    [unused,ms] = max(score);
%                     if ms == 1 || ms == length(score)
%                         disp('problem')
%                     end
                    f0s(a).meanf0(end+1) = f0s(a).meanf0(end) + delta(ms);
                    
                    g = 1;
                    while g <= length(active)
                        ag = active(g);
                        if ag == a
                            g = g + 1;
                            continue
                        end
                        if abs(log2(f0s(ag).meanf0(end) / f0s(a).meanf0(end))) < tolratio
                            active(g) = [];
                            acts = acts - 1;
                        else
                            g = g + 1;
                        end
                    end
                    
                    dms = dh(round(ifk(ms,:)*10));
                    f0s(a).mgs(end+1,1:length(dms)) = dms;
                    
                    if dms(2)/max(dms([1,3])) > 1.3 && ...
                            dms(4)/max(dms([3,5])) > 1.3 && ...
                            dms(6)/max(dms([5,7])) > 1.3
                        l = 2;
                        found = 0;
                        index = 0;
                        for g = 1:length(active)
                            ag = active(g);
                            if ag == a
                                continue
                            end
                            if ~index && f0s(a).meanf0(end) * l < f0s(ag).meanf0(end)
                                index = g;
                            end
                            if abs(log2(f0s(ag).meanf0(end) / f0s(a).meanf0(end) / l)) < tolratio
                                found = 1;
                                break
                            end
                        end
                        if ~found
                            f0s(end+1).meanf0(h) = f0s(a).meanf0(h) * l;
                            f0s(end).mgs = dms(2:2:end);
                            
                            res = 0;
                            for q = 2:length(f0s(end).mgs)
                                res = res + f0s(end).mgs(q) * f0s(end).mgs(q-1);
                            end
                            f0s(end).globmg(h) = res * f0s(end).mgs(1);
                            if res > f0s(end).maxmg
                                f0s(end).maxmg = res;
                            end
                            
                            f0s(end).harm = f0s(a).harm(l:l:end);
                            if ~index
                                active(end+1) = length(f0s);
                            else
                                active = [active(1:index-1),length(f0s),active(index:end)];
                                acts = acts + 1;
                            end
                        end
                    end
                    
                    if size(f0s(a).mgs,1) <= 6
                        f0s(a).sco(end+1,:) = zeros(1,length(dms));
                    else
                        g = size(f0s(a).sco,1) + 1;
                        scog = zeros(1,length(dms));
                        for l = 1:length(dms)
                            scog(l) = max(f0s(a).sco(g-1,l)*.75,...
                                        (f0s(a).mgs(g,l) - f0s(a).mgs(g-5,l)) * f0s(a).mgs(g,l));
                            f0s(a).sco(g,l) = scog(l);
                        end
                        
                        for l = 2:3
                            if length(f0s(a).meanf0) == h && ...
                                f0s(a).meanf0(h) * l > 1500
                                continue
                            end
                            
                            scogl = scog;
                            comb = 0;
                            for g = 1:floor(length(scogl)/l) - 1
                                comb = max(comb,min(scogl(l*g),scog(l*(g+1))));
                            end
                            %comb = sum(scogl(l:l:end));
                            scogl(l:l:end) = 0;
                            othr = max(scogl);
                            if comb - othr > 100
                                found = 0;
                                index = 0;
                                for g = 1:length(active)
                                    ag = active(g);
                                    if ag == a
                                        continue
                                    end
                                    if ~index && f0s(a).meanf0(end) * l < f0s(ag).meanf0(end) % Should be moved below??
                                        index = g;
                                    end
                                    if abs(log2(f0s(ag).meanf0(end) / f0s(a).meanf0(end) / l)) < tolratio
                                        found = 1;
                                        break
                                    end
                                end
                                if ~found
                                    f0s(end+1).meanf0(h) = f0s(a).meanf0(h) * l;
                                    f0s(end).mgs = dms(2:2:end);
                                    
                                    res = 0;
                                    for q = 2:length(f0s(end).mgs)
                                        %     if mod(i,2) % odd harmonics
                                        %         res = res + max(amps(i) * amps(i-2), amps(i) * amps(i-1));
                                        %     else        % even harmonics
                                        res = res + f0s(end).mgs(q) * f0s(end).mgs(q-1);
                                        %     end
                                    end
                                    f0s(end).globmg(h) = res * f0s(end).mgs(1);
                                    if res > f0s(end).maxmg
                                        f0s(end).maxmg = res;
                                    end
                                    
                                    f0s(end).harm = f0s(a).harm(l:l:end);
                                    if ~index
                                        active(end+1) = length(f0s);
                                    else
                                        active = [active(1:index-1),length(f0s),active(index:end)];
                                        acts = acts + 1;
                                    end
                                end
                            end
                                    
                        end
                    end
                    
                    res = 0;
                    for l = 2:length(dms)
                        %     if mod(i,2) % odd harmonics
                        %         res = res + max(amps(i) * amps(i-2), amps(i) * amps(i-1));
                        %     else        % even harmonics
                        res = res + dms(l) * dms(l-1);
                        %     end
                    end
                    f0s(a).globmg(end+1) = res * dms(1);
                    if res > f0s(a).maxmg
                        f0s(a).maxmg = res;
%                     elseif res < f0s(a).maxmg / 100
%                         active(k) = [];
%                         continue
                    end
                    
                    if ~isempty(ph)
                        found = 0;
                        for l = 1:length(f0s(a).harm)
                            %                         if ~f0s(a).harm(l)
                            %                             continue
                            %                         end
                            
                            %                         [unused,mm] = min(abs(ifk(ms,l) - ph0));
                            %                         ph(mm) = [];
                            %                         ph0(mm) = [];
                            
                            %                         if isempty(ph)
                            %                             break
                            %                         end
                            
                            
                            
                            fl = f0s(a).meanf0(end) * l;
                            xx = find(ph > fl / ratio,1);
                            foundl = 0;
                            if isempty(xx)
                                if abs(log2(fl / ph(end))) < tolratio
                                    foundl = length(av);
                                else
                                    continue
                                end
                            end
                            yy = find(ph(xx:end) > fl * ratio,1);
                            idf0 = find(fj(:,h) >= fl,1);
                            if isempty(yy) || yy == 1
                                if xx == 1
                                    dd = abs(log2(ph(xx)/fl));
                                    yy = 2;
                                else
                                    [dd, yy] = min([abs(log2(ph(xx-1)/fl)),...
                                        abs(log2(ph(xx)/fl))]);
                                end
%                                 dd < tolratio
%                                  dij(idf0,h) / pvh(xx+yy-2)
                                if dd < tolratio && dij(idf0,h) / pvh(xx+yy-2) > .5
                                    foundl = xx+yy-2;
                                end
                            else
                                foundl = [];
                                for z = xx:xx+yy-2
                                    if 1 % dij(idf0,h) / pvh(z) > .5
                                        foundl(end+1) = z;
                                    end
                                end
                            end
                            if foundl
                                %                             if f0s(a).harm(l) > 0
                                av(foundl) = 0;
                                if length(foundl) > 1
                                    if f0s(a).harm(l) > 0
                                        [unused, wh] = min(abs(pp{i}{j}{h-1}(f0s(a).harm(l)) - ph(foundl)));
                                    else
                                        [unused, wh] = max(dij(idf0,h) / pvh(foundl));
                                    end
                                    foundl = foundl(wh);
                                end
                                %                                 if 1 % dij(idf0,h) / pvh(foundl) > .5
                                %                                     if 1 %~(l > 1 && size(f0s(a).mgs,1) > 6 ...
                                % %                                          && diff(f0s(a).mgs([end-5,end],l)) - max(diff(f0s(a).mgs([end-5,end],1:l-1))) > .3 ...
                                % %                                          ...&& max(diff(f0s(a).mgs([end-5,end],1:l-1))) < .3 ...
                                % %                                          && max(f0s(a).mgs(end,1:l-1)) - f0s(a).mgs(end,l) < 1)
                                %                                         % f0s(a).harm(l) > 0
                                % %                                         av(foundl) = 0;
                                %                                     else
                                %                                         1;
                                %                                     end
                                found = 1;
                                f0s(a).harm(l) = foundl;
                                %                                 else
                                %                                     f0s(a).harm(l) = 0;
                                %                                 end
                                %                             else
                                %                                 if length(foundl) > 1
                                %                                     [unused,wh] = max(dij(idf0,h) / pvh(foundl));
                                %                                     foundl = foundl(wh);
                                %                                 end
                                %                                 if dij(idf0,h) / pvh(foundl) > .5
                                %                                     av(foundl) = 0;
                                %                                     found = 1;
                                %                                 end
                                %                             end
                            else
                                f0s(a).harm(l) = 0;
                            end
                        end
                        
                        if (~found && ...
                                (isempty(f0s(a).sub) || ...
                                ~ismember(f0s(a).sub(1),active))) ...
                                || isempty(find(f0s(a).harm,1)) || find(f0s(a).harm,1) > 3
                            active(k) = [];
                            acts = acts - 1;
                        else
                            k = k + 1;
                        end
                    end
                end

                if ~isempty(find(av))
                    count = ones(1,nharm);
                    fk2f0s = fk2f0(ph(count),1:length(count),beta);
                    sets = struct('f0s',{},'fs',{},'harm',{},'nh',{},'pidx',{},'fidx',{},'mgs',{},'meanf0',{},'globmg',{},'diff',{});
                    isharmonic = cell(1,length(ph));
                    lastfidx = [];
                    while ~isempty(count)
%                         count
%                         sets
%                         if ~isempty(sets)
%                             sets(1)
%                         end
%                         f0s
%                         pause
                        [unused,idx] = min(fk2f0s);
                        pidx = count(idx);
                        if 1 %idx > 1 || av(pidx)
                            fidx = ph(pidx);
                            f0k = fk2f0(fidx,idx,beta);
                            l = 1;
                            transfered = false;
                            if 1 %h == 1
                                diffx = 1;
                            else
                                idf0 = find(fj(:,h) >= fidx,1);
                                diffx = dij(idf0,h) - min(dij(idf0,max(1,h-5):h-1)) > 1;
                            end
                            while l <= length(sets)
                                condition1 = 0;
%                                 if sets(l).nh < 10
                                    nh = sets(l).nh;
                                    
%                                     nh = nh + 1;


%                                     if idx == 1 && f0k < 500
%                                         k1 = find(fj(:,h) >= f0k,1);
%                                         k2 = find(fj(:,h) >= meanf0,1);
%                                         if dij(k2,h) / dij(k1,h) > .7
%                                             condition1 = 1; %%% Not mentioned in paper
%                                         end
%                                     end
%                                 end
                                if (condition1 || log2(f0k/sets(l).f0s(1)) < tolratio) && ...quartertone) && ... semitone && ...   % f0k/sets(l).f0s(end) ? f0k/sets(l).meanf0
                                        (idx > length(sets(l).fidx) || ~sets(l).fidx(idx))
                                    nh = nh + 1;
                                    sets(l).f0s(nh) = f0k;
                                    sets(l).fs(nh) = fidx;
                                    sets(l).harm(nh) = idx;
                                    sets(l).nh = nh;
                                    sets(l).fidx(idx) = fidx;
                                    sets(l).pidx(idx) = pidx;
                                    sets(l).mgs(idx) = pvh(pidx);
                                    sets(l).diff(idx) = diffx;
                                    %sets(l).meanf0 = meanf0;
                                    %                                     idf0 = find(fj(:,h) >= sets(l).meanf0,1);
                                    %                                     idf1 = find(fj(:,h) >= sets(l).meanf0 / quartertone_ratio,1);
                                    %                                     idf2 = find(fj(:,h) >= sets(l).meanf0 * quartertone_ratio,1) - 1;
                                    %                                     d0 = max(dij(idf1:max(idf0,idf2),h));
                                    %                                     if ~sets(l).mgs(1) && sets(l).mgs(2) %max(sets(l).mgs(2:3))
                                    %                                         sets(l).mgs(1) = d0;
                                    %                                     end
                                    %                                     sets(l).globmg = globmg(sets(l).mgs) * d0;
                                    l = l + 1;
                                else
                                    ff = sets(l).f0s(1:nh)*sets(l).harm(1:nh)'/sum(sets(l).harm(1:nh));
%                                     ff = sets(l).meanf0;
                                    if ff > 1100 || find(sets(l).mgs,1) > 3
                                        sets(l) = [];
                                        continue
                                    end
                                    pen = zeros(1,nh);
                                    for p = 1:nh
                                        pen(sets(l).harm(p)) = (min(ff,sets(l).f0s(p)) / max(ff,sets(l).f0s(p))) .^10;
                                    end
                                    d0 = zeros(1,length(sets(l).mgs));
                                    for p = 1:min(2,length(sets(l).mgs))
                                        if p == 1 || ~sets(l).mgs(p)
                                            idf0 = find(fj(:,h) >= ff * p,1);
                                            if idf0 == 1
                                                d0(p) = dij(idf0,h); %%% USE DH INSTEAD?
                                            else
                                                d0(p) = (dij(idf0,h) * (ff*p - fj(idf0-1,h)) ...
                                                    + dij(idf0-1,h) * (fj(idf0,h) - ff*p)) ...
                                                    / (fj(idf0,h) - fj(idf0-1,h));
                                            end
                                            if ~sets(l).mgs(p) && d0(p) > mh / 10
                                                sets(l).mgs(p) = d0(p);
                                                sets(l).pidx(p) = -1;
                                            end
                                        end
                                    end
                                    sets(l).globmg = globmg(sets(l).mgs.*pen,sets(l).pidx) * d0(1);
                                    
                                    if ~transfered && sets(l).globmg > 0 .......400 ....5 ...
                                            && sets(l).mgs(1) ...
                                            ...&& sets(l).mgs(2) ...
                                            ...&& length(sets(l).pidx) >= 3 && sets(l).pidx(3) ...
                                            && ((sets(l).pidx(1) > 0 && av(sets(l).pidx(1))))... || ...
                                                ...(sets(l).pidx(2) > 0 && av(sets(l).pidx(2))) || ...
                                                ...(length(sets(l).pidx) >= 3 && sets(l).pidx(3) > 0 && av(sets(l).pidx(3)))) ...
                                            && ~isempty(find(sets(l).diff,1))
                                            % (sets(l).pidx(1) || sets(l).pidx(3)) ...
                                        %                                                && ((length(sets(l).mgs) >= 3 ... %% Does not work is note exists at harmonic!
                                        %                                                     && sets(l).mgs(3)) ...
                                        %                                                    || (length(sets(l).mgs) >= 4 ...
                                        %                                                        && sets(l).mgs(4)))
                                        integrate = 1;
                                        if ~isempty(lastfidx)
                                            for p = 1:min(length(lastfidx),length(sets(l).fidx))
                                                if ...lastfidx(p) && ...
                                                        ...sets(l).fidx(p) && ...
                                                        lastfidx(p) ~= sets(l).fidx(p)
                                                    for q = p+1:min(length(lastfidx),length(sets(l).fidx))
                                                        if ~lastfidx(q) && ~sets(l).fidx(q)
                                                            continue
                                                        end
                                                        if lastfidx(q) == sets(l).fidx(q)
                                                            if f0s(end).globmg(end) < sets(l).globmg
                                                                for r = 1:length(lastpidx)
                                                                    pidxr = lastpidx(r);
                                                                    if pidxr > 0
                                                                        isharmonic{pidxr}(end) = [];
                                                                    end
                                                                end
                                                                active(active == length(f0s)) = [];
                                                                f0s(end) = [];
                                                                integrate = 1;
                                                            else
                                                                integrate = 0;
                                                            end
                                                            break
                                                        end
                                                    end
                                                    break
                                                end
                                            end
                                        end
                                        if integrate
                                            m = sets(l).globmg;
                                            found = 0;
                                            
                                            for k = 1:length(active)
                                                a = active(k);
                                                included = 1;
                                                for p = 1:length(f0s(a).harm)
                                                    if ~f0s(a).harm(p) 
                                                        continue
                                                    end
                                                    [test,loc] = ismember(f0s(a).harm(p),sets(l).pidx);
                                                    if ~test || loc == p
                                                        included = 0;
                                                        break
                                                    end
                                                end
                                                if included
                                                    f0s(a).meanf0(end) = ff;
                                                    f0s(a).globmg(end) = m;
                                                    f0s(a).harm = sets(l).pidx;
                                                    found = 1;
                                                end
                                                fk = f0s(a).meanf0(end);
                                                qr = fk / ff;
                                                rqr = round(qr);
                                                if rqr > 1 && abs(qr-rqr) < .05
                                                    idfk = find(fj(:,h) >= fk,1);
                                                    idf = find(fj(:,h) >= f,1);
                                                    if dij(idf,h) > dij(idfk,h)
                                                        f0s(a).meanf0(end) = f;
                                                        f0s(a).globmg(end) = m;
                                                        found = 1;
                                                        break
                                                    end
                                                    if size(f0s(a).mgs,1) < 10
                                                        f0s(a) = [];
                                                        active(k) = [];
                                                        break
                                                    end
                                                end
                                            end

                                            if ~found
                                                f0s(end+1).meanf0(h) = ff;
                                                f0s(end).globmg(h) = m;
                                                f0s(end).maxmg = m;
                                                f0s(end).sub = [];
                                                f0s(end).super = [];
                                                f0s(end).mgs = sets(l).mgs;
                                                f0s(end).harm = sets(l).pidx;
                                                f0s(end).sco = [];
                                                transfered = true;
                                                
                                                index = 0;
                                                for g = 1:length(active)
                                                    ag = active(g);
                                                    if ~index && ff < f0s(ag).meanf0(end)
                                                        index = g;
                                                    end
                                                end
                                                
                                                if ~index
                                                    active(end+1) = length(f0s);
                                                else
                                                    active = [active(1:index-1),length(f0s),active(index:end)];
                                                    acts = acts + 1;
                                                end
                                                
                                                for p = sets(l).pidx
                                                    if p > 0
                                                        av(p) = 0;
                                                    end
                                                end
                                            end
                                            lastfidx = sets(l).fidx;
                                            lastpidx = sets(l).pidx;
                                            for p = 1:length(lastpidx)
                                                pidxp = lastpidx(p);
                                                if pidxp < 1
                                                    continue
                                                end
                                                for q = 1:length(isharmonic{pidxp})
                                                    if isharmonic{pidxp}(q).index == p || mod(isharmonic{pidxp}(q).index,p)
                                                        continue
                                                    end
                                                    f0s(end).sub(end+1) = isharmonic{pidxp}(q).f0s;
                                                end
                                                isharmonic{pidxp}(end+1).index = p;
                                                isharmonic{pidxp}(end).f0s = length(f0s);
                                            end
                                            if ~isempty(f0s(end).sub)
                                                active(active == length(f0s)) = [];
                                                f0s(end) = [];
                                            end
                                        end
                                    end
                                    sets(l) = [];
                                end
                            end
                            if 1 %av(pidx)
                                sets(end+1).f0s = [f0k,zeros(1,9)];
                                sets(end).fs = [fidx,zeros(1,9)];
                                sets(end).harm = [idx,zeros(1,9)];
                                sets(end).nh = 1;
                                sets(end).fidx(idx) = fidx;
                                sets(end).pidx(idx) = pidx;
                                sets(end).mgs(idx) = pvh(pidx);
                                sets(end).meanf0 = f0k;
                                sets(end).diff(idx) = diffx;
                            end
                        end
                        if pidx == length(ph)
                            count(idx) = [];
                            fk2f0s(idx) = [];
                        else
                            pidx = pidx + 1;
                            count(idx) = pidx;
                            fk2f0s(idx) = ph(pidx)/idx/sqrt(1+(idx-1).^2*beta);
                        end
                    end
                end
            end
        end
    end
    k = 1;
    while k <= length(f0s)
        if length(find(f0s(k).meanf0)) < 1 %10 %5
            active(active == k) = [];
            f0s(k) = [];
        else
            k = k + 1;
        end
    end
    
    figure, hold on
    for k = 1:length(f0s)
        if 1 %isempty(f0s(k).sub)
            first = 0;
            for i = 1:length(f0s(k).meanf0)
                if f0s(k).meanf0(i)
                    if ~first
                        first = 1;
                        plot(fp{1}{1}(1,i),f0s(k).meanf0(i),'+r')
                    end
                    c = max(0,1-max(0,log10(f0s(k).globmg(i)))/5);
                    plot(fp{1}{1}(1,i),f0s(k).meanf0(i),'.','Color',[c c c])
                end
            end
            plot(fp{1}{1}(1,length(f0s(k).meanf0)),f0s(k).meanf0(end),'xy')
        end
    end
    
    figure, hold on
    for k = 1:length(f0s)
        if 1 %isempty(f0s(k).sub)
            plot(log10(f0s(k).globmg))
        end
    end
    1
    %%
elseif option.harmonics == 2 % VERSION 20.3.2018
    pp = get(x,'PeakPos');
    f = get(x,'Pos');
    dd = get(x,'Data');
    beta = 0; %.001; %.005;
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            partials = {};
            fj = f{i}{j};
            dij = dd{i}{j};
            f0s = struct('meanf0',{},'f0s',{},'globmg',{},'mgs',{},'sub',{},'partials',{});
            recycle = [];
            for h = 1:length(pf{i}{j})
                if ~mod(h,100)
                    h/length(pf{i}{j})
                end
%                 h,pf{i}{j}{h}
                k = 1;
                ph = pp{i}{j}{h};
                pfree = [];
                whichpartial = [];
                active = [];
                while k <= length(ph)
                    fk = pf{i}{j}{h}(k);
                    pk = ph(k);
                    if fk < option.mi || fk > option.ma
                        continue
                    end
                    if isempty(partials)
                        partials{1}.index = h;
                        partials{1}.f = fk;
                        partials{1}.m = pa{i}{j}{h}(k);
                        partials{1}.k = k;
                        partials{1}.integrated = [];
                        whichpartial(end+1) = 1;
                        pfree(end+1) = pk;
                    else
                        d = zeros(length(partials),1);
                        for l = 1:length(partials)
                            if partials{l}.index(end) < h-1
                                d(l) = Inf;
                            else
                                if length(partials{l}.f) > 1 && ...
                                        partials{l}.index(end) == h
                                    fend(l) = partials{l}.f(end-1);
                                else
                                    fend(l) = partials{l}.f(end);
                                end
                                d(l) = abs(log2(fend(l) / fk));
                            end
                        end
                        [mv,mi] = min(d);
                        if (k < length(ph) && abs(log2(fend(l) / pf{i}{j}{h}(k+1))) < mv) ...
                                || mv > 1/24
                            r = find(recycle,1);
                            if isempty(r)
                                r = length(partials) + 1;
                            else
                                recycle(r) = 0;
                            end
                            partials{r}.index = h;
                            partials{r}.f = fk;
                            partials{r}.m = pa{i}{j}{h}(k);
                            partials{r}.k = k;
                            partials{r}.integrated = [];
%                             found = 0;
%                             for l = 1:length(f0s)
%                                 d = fk/f0s(l).meanf0(end);
%                                 rd = round(d);
%                                 if rd && abs(d-rd) < .03 && ...
%                                         (length(f0s(l).partials) < rd || ~f0s(l).partials(rd))
%                                     f0s(l).partials(rd) = r;
%                                     f0s(l).f0s(rd) = fk;
%                                     f0s(l).mgs(rd) = pa{i}{j}{h}(k);
%                                     found = 1;
%                                     partials{r}.integrated.whichf0 = l;
%                                     partials{r}.integrated.harm = rd;
%                                     break
%                                 end
%                             end
%                             if ~found
                                whichpartial(end+1) = r;
                                pfree(end+1) = pk;
%                             end
                        else
                            if partials{mi}.index(end) < h
                                partials{mi}.index(end+1) = h;
                                partials{mi}.f(end+1) = fk;
                                partials{mi}.m(end+1) = pa{i}{j}{h}(k);
                                partials{mi}.k = k;
                                
                                pi = partials{mi}.integrated;
                                if isempty(pi)
                                    pfree(end+1) = pk;
                                    whichpartial(end+1) = mi;
                                else
                                    q = pi.whichf0;
                                    qr = fk / f0s(q).meanf0(end);
                                    rqr = round(qr);
                                    if abs(qr-rqr) > .1
                                        for l = 1:length(f0s)
                                            if l == q
                                                continue
                                            end
                                            d = fk/f0s(l).meanf0(end);
                                            rd = round(d);
                                            if rd && abs(d-rd) < .1 && ...
                                                    (length(f0s(l).partials) < rd || ~f0s(l).partials(rd))
                                                f0s(l).partials(rd) = mi;
                                                f0s(l).f0s(rd) = fk;
                                                f0s(l).mgs(rd) = pa{i}{j}{h}(k);
                                                partials{mi}.integrated.whichf0 = l;
                                                partials{mi}.integrated.harm = rd;
                                                f0s(q).partials(rd) = 0;
                                                f0s(q).f0s(rd) = 0;
                                                f0s(q).mgs(rd) = 0;
                                                break
                                            end
                                        end
                                    end
                                    f0s(q).f0s(pi.harm) = fk;
                                    f0s(q).mgs(pi.harm) = pa{i}{j}{h}(k);
                                    if ~ismember(q,active)
                                        active(end+1) = q;
                                    end
                                end
                            elseif partials{mi}.m(end) < pa{i}{j}{h}(k)
                                partials{mi}.f(end) = fk;
                                partials{mi}.m(end) = pa{i}{j}{h}(k);
                                partials{mi}.k = k;
                                pi = partials{mi}.integrated;
                                if isempty(pi)
                                    pfree(end) = pk;
                                else
                                    q = pi.whichf0;
                                    f0s(q).f0s(pi.harm) = fk;
                                    f0s(q).mgs(pi.harm) = pa{i}{j}{h}(k);
                                end
                            end
                            %whichpartial(end+1) = mi;
                        end
                    end
                    k = k+1;
                end
                k = 1;
                while k <= length(active)
                    a = active(k);
                    hh = find(f0s(a).f0s);
                    f0s(a).meanf0(h) = sum(f0s(a).f0s)/sum(hh);
                    f0s(a).f0s = [];
                    d0 = [];
                    for p = 1:min(2,length(f0s(a).mgs))
                        if p == 1 || ~f0s(a).mgs(p)
                            idf0 = find(fj(:,h) >= f0s(a).meanf0(h) * p,1);
                            d0(p) = dij(idf0,h);
                            if ~f0s(a).mgs(p) && d0(p) > max(f0s(a).mgs) / 20
                                f0s(a).mgs(p) = d0(p);
                            end
                        end
                    end
                    f0s(a).globmg(h) = globmg(f0s(a).mgs,f0s(a).partials) * d0(1);
                    f0s(a).mgs = [];
                    if f0s(a).globmg(h)
                        k = k + 1;
                    else
                        active(k) = [];
                        for p = 1:length(f0s(a).partials)
                            ppp = f0s(a).partials(p);
                            if ppp
                                partials{ppp}.integrated = [];
                            end
                            f0s(a).partials(p) = 0;
                        end
                    end
                end
%                 if ~isempty(f0s)
%                     k = 1;
%                     while k <= length(pfree)
%                         pk = pfree(k);
%                         k = k + 1;
%                     end
%                 end

                k = 1;
                while k <= length(pfree)
                    fk = fj(pfree(k));
                    r = whichpartial(k);
                    for l = 1:length(f0s)
                        d = fk/f0s(l).meanf0(end);
                        rd = round(d);
                        if rd && abs(d-rd) < .1 && ...
                                (length(f0s(l).partials) < rd || ~f0s(l).partials(rd))
                            f0s(l).partials(rd) = r;
                            f0s(l).f0s(rd) = fk;
                            f0s(l).mgs(rd) = partials{r}.m(end);
                            partials{r}.integrated.whichf0 = l;
                            partials{r}.integrated.harm = rd;
                            pfree(k) = [];
                            whichpartial(k) = [];
                            k = k - 1;
                            break
                        end
                    end
                    k = k + 1;
                end

                active = [];
                if ~isempty(pfree)
                    count = ones(1,10);
                    fk2f0s = fk2f0(fj(pfree(count)),1:length(count),beta);
                    sets = struct('f0s',{},'fs',{},'harm',{},'nh',{},'pidx',{},'fidx',{},'mgs',{},'meanf0',{},'globmg',{});
                    isharmonic = cell(1,length(pfree));
                    lastfidx = [];
                    lastpidx = [];
                    while ~isempty(count)
                        [unused,idx] = min(fk2f0s);
                        pidx = count(idx);
                        fidx = pfree(pidx);
                        f0k = fk2f0(fj(fidx,h),idx,beta);
                        l = 1;
                        transfered = false;
                        while l <= length(sets)
                            if ((idx == 1 && f0k < 500) || ...
                                    log2(f0k/sets(l).f0s(1)) < .06) && ...quartertone) && ... semitone && ...   % f0k/sets(l).f0s(end) ? f0k/sets(l).meanf0
                                    (idx > length(sets(l).fidx) || ~sets(l).fidx(idx))
                                nh = sets(l).nh + 1;
                                sets(l).f0s(nh) = f0k;
                                sets(l).fs(nh) = fj(fidx,h);
                                sets(l).harm(nh) = idx;
                                sets(l).nh = nh;
                                sets(l).fidx(idx) = fidx;
                                sets(l).pidx(idx) = pidx;
                                sets(l).mgs(idx) = dij(fidx,h);
                                sets(l).meanf0 = sets(l).f0s(1:nh)*sets(l).harm(1:nh)'/sum(sets(l).harm(1:nh));
                                %                                     idf0 = find(fj(:,h) >= sets(l).meanf0,1);
                                %                                     idf1 = find(fj(:,h) >= sets(l).meanf0 / quartertone_ratio,1);
                                %                                     idf2 = find(fj(:,h) >= sets(l).meanf0 * quartertone_ratio,1) - 1;
                                %                                     d0 = max(dij(idf1:max(idf0,idf2),h));
                                %                                     if ~sets(l).mgs(1) && sets(l).mgs(2) %max(sets(l).mgs(2:3))
                                %                                         sets(l).mgs(1) = d0;
                                %                                     end
                                %                                     sets(l).globmg = globmg(sets(l).mgs) * d0;
                                l = l + 1;
                            else
                                d0 = zeros(1,length(sets(l).mgs));
                                if find(sets(l).mgs,1) > 3
                                    sets(l) = [];
                                    continue
                                end
                                for p = 1:min(2,length(sets(l).mgs))
                                    if p == 1 || ~sets(l).mgs(p)
                                        idf0 = find(fj(:,h) >= sets(l).meanf0 * p,1);
                                        %                                             idf1 = find(fj(:,h) >= sets(l).meanf0 * p / quartertone_ratio,1);
                                        %                                             idf2 = find(fj(:,h) >= sets(l).meanf0 * p * quartertone_ratio,1) - 1;
                                        %                                             d0(p) = max(dij(idf1:max(idf0,idf2),h));
                                        d0(p) = dij(idf0,h);
                                        if ~sets(l).mgs(p) && d0(p) > max(sets(l).mgs) / 10
                                            sets(l).mgs(p) = d0(p);
                                        end
                                    end
                                end
                                sets(l).globmg = globmg(sets(l).mgs,sets(l).pidx) * d0(1);

                                if ~transfered && sets(l).globmg > .5 && sets(l).mgs(1) && sets(l).mgs(2)
                                    integrate = 1;
                                    if ~isempty(lastfidx)
                                        for p = 1:min(length(lastfidx),length(sets(l).fidx))
                                            if ...lastfidx(p) && ...
                                                    ...sets(l).fidx(p) && ...
                                                    lastfidx(p) ~= sets(l).fidx(p)
                                                for q = p+1:min(length(lastfidx),length(sets(l).fidx))
                                                    if ~lastfidx(q) && ~sets(l).fidx(q)
                                                        continue
                                                    end
                                                    if lastfidx(q) == sets(l).fidx(q)
                                                        if f0s(end).mgs < sets(l).globmg
                                                            for r = 1:length(lastpidx)
                                                                pidxr = lastpidx(r);
                                                                if pidxr
                                                                    isharmonic{pidxr}(end) = [];
                                                                end
                                                            end
                                                            f0s(end) = [];
                                                            integrate = 1;
                                                        else
                                                            integrate = 0;
                                                        end
                                                        break
                                                    end
                                                end
                                                break
                                            end
                                        end
                                    end
                                    if integrate
                                        f0s(end+1).meanf0(h) = sets(l).meanf0;
                                        f0s(end).globmg(h) = sets(l).globmg;
                                        f0s(end).partials = zeros(1,length(sets(l).pidx));
                                        for p = 1:length(sets(l).pidx)
                                            if sets(l).pidx(p)
                                                f0s(end).partials(p) = whichpartial(sets(l).pidx(p));
                                            end
                                        end
                                        f0s(end).harm = sets(l).harm;
                                        f0s(end).sub = [];
                                        lastpidx = sets(l).pidx;
                                        lastfidx = sets(l).fidx;
                                        transfered = true;
                                        active(end+1) = length(f0s);
                                        for p = 1:length(lastpidx)
                                            pidxp = lastpidx(p);
                                            if ~pidxp
                                                continue
                                            end
                                            for q = 1:length(isharmonic{pidxp})
                                                if isharmonic{pidxp}(q).index == p || mod(isharmonic{pidxp}(q).index,p)
                                                    continue
                                                end
                                                f0s(end).sub(end+1) = isharmonic{pidxp}(q).f0s;
                                            end
                                            isharmonic{pidxp}(end+1).index = p;
                                            isharmonic{pidxp}(end).f0s = length(f0s);
                                        end
                                    end
                                end
                                sets(l) = [];
                            end
                        end
                        sets(end+1).f0s = [f0k,zeros(1,9)];
                        sets(end).fs = [fj(fidx,h),zeros(1,9)];
                        sets(end).harm = [idx,zeros(1,9)];
                        sets(end).nh = 1;
                        sets(end).fidx(idx) = fidx;
                        sets(end).pidx(idx) = pidx;
                        sets(end).mgs(idx) = dij(fidx,h);
                        sets(end).meanf0 = f0k;
                        %                             idf0 = find(fj(:,h) >= f0k,1);
                        %                             idf1 = find(fj(:,h) >= f0k / quartertone_ratio,1);
                        %                             idf2 = find(fj(:,h) >= f0k * quartertone_ratio,1) - 1;
                        %                             d0 = max(dij(idf1:max(idf0,idf2),h));
                        %                             if idx == 1
                        %                                 sets(end).globmg = globmg(sets(end).mgs) * d0;
                        %                             elseif idx == 2 %<= 3
                        %                                 sets(end).mgs(1) = d0;
                        %                                 sets(end).globmg = globmg(sets(end).mgs) * d0;
                        %                             else
                        %                                 sets(end).globmg = 0;
                        %                             end

                        if pidx == length(pfree)
                            count(idx) = [];
                            fk2f0s(idx) = [];
                        else
                            pidx = pidx + 1;
                            count(idx) = pidx;
                            fk2f0s(idx) = fj(pfree(pidx))/idx/sqrt(1+(idx-1).^2*beta);
                        end
                    end
                    for k = active
                        for l = 1:length(f0s(k).partials)
                            p = f0s(k).partials(l);
                            if p && isempty(partials{p}.integrated)
                                partials{p}.integrated.whichf0 = k;
                                partials{p}.integrated.harm = l;
                            end
                        end
                    end
                end
                for k = 1:length(partials)
                    if partials{k}.index < h
                        recycle(k) = 1;
                        pi = partials{k}.integrated;
                        if ~isempty(pi)
                            f0s(pi.whichf0).partials(pi.harm) = 0;
                        end
                        partials{k}.integrated = [];
                    end
                end
            end
        end
    end
    k = 1;
    while k <= length(f0s)
        if length(find(f0s(k).meanf0)) < 5
            f0s(k) = [];
        else
            k = k + 1;
        end
    end
    
    figure, hold on
    for k = 1:length(f0s)
        plot(f0s(k).meanf0,'+')
    end
    
    figure, hold on
    for k = 1:length(f0s)
        plot(log10(f0s(k).globmg))
    end
elseif option.harmonics
    pp = get(x,'PeakPos');
    pv = get(x,'PeakVal');
    pm = get(x,'PeakMode');
    dd = get(x,'Data');
    f = get(x,'Pos');
    quartertone = 1/24;
    beta = .001; %.005;
    for i = 1:length(pf)
        for j = 1:length(pf{i})                
            f0s = [];
            sumh = 0;
            notes = [];
            active = [];
            nmat = zeros(0,7);
            
            dij = dd{i}{j};
            m = zeros(size(dij));
            fj = f{i}{j};
            for h = 1:length(pf{i}{j})
%                 h,pf{i}{j}{h}
                if ~mod(h,1000)
                    h/length(pf{i}{j}),length(f0s)
%                     toc,tic
                end
                ph = pp{i}{j}{h};
                if isempty(ph)
                    continue
                end
                sumh = max(sumh*.8,sum(dd{i}{j}(:,h)));
                for k = 1:length(f0s)
                    % Removing old f0s
                    if f0s(k).h < h - 5
                        %f0s(k) = [];
                        f0s(k).fnow = NaN;
                        l = 1;
                        while l <= length(active)
                            if notes(active(l)).f0 == k
                                dur = fp{i}{j}(1,h) - nmat(active(l),1);
                                nmat(active(l),[2 7]) = dur;
                                active(l) = [];
                            else
                                if notes(active(l)).f0 > k
                                    notes(active(l)).f0 = notes(active(l)).f0 - 1;
                                end
                                l = l + 1;
                            end
                        end
%                         for l = 1:length(f0s)
%                             g = 1;
%                             while g <= length(f0s(l).sub)
%                                 if f0s(l).sub(g) == k
%                                     f0s(l).sub(g) = [];
%                                 else
%                                     if f0s(l).sub(g) > k
%                                         f0s(l).sub(g) = f0s(l).sub(g) - 1;
%                                     end
%                                     g = g + 1;
%                                 end
%                             end
%                         end

%                     else
%                         k = k + 1;
                    end
                end
                
                N = size(dij,1);
                count = ones(1,10);
                fk2f0s = fk2f0(fj(ph(count)),1:length(count),beta);
                sets = struct('f0s',{},'fs',{},'harm',{},'nh',{},'pidx',{},'fidx',{},'mgs',{},'meanf0',{},'globmg',{},'sub',{});
                finalset = sets;
                isharmonic = cell(1,length(ph));
                while ~isempty(count)
                    [unused,idx] = min(fk2f0s);
                    pidx = count(idx);
                    fidx = ph(pidx);
                    f0k = fk2f0(fj(fidx,h),idx,beta);
                    l = 1;
                    transfered = false;
                    while l <= length(sets)
                        if ((idx == 1 && f0k < 500) || ...
                                log2(f0k/sets(l).f0s(1)) < .06) && ...quartertone) && ... semitone && ...   % f0k/sets(l).f0s(end) ? f0k/sets(l).meanf0
                                (idx > length(sets(l).fidx) || ~sets(l).fidx(idx))
                            nh = sets(l).nh + 1;
                            sets(l).f0s(nh) = f0k;
                            sets(l).fs(nh) = fj(fidx,h);
                            sets(l).harm(nh) = idx;
                            sets(l).nh = nh;
                            sets(l).fidx(idx) = fidx;
                            sets(l).pidx(idx) = pidx;
                            sets(l).mgs(idx) = dij(fidx,h);
                            sets(l).meanf0 = sets(l).f0s(1:nh)*sets(l).harm(1:nh)'/sum(sets(l).harm(1:nh));
                            %                                     idf0 = find(fj(:,h) >= sets(l).meanf0,1);
                            %                                     idf1 = find(fj(:,h) >= sets(l).meanf0 / quartertone_ratio,1);
                            %                                     idf2 = find(fj(:,h) >= sets(l).meanf0 * quartertone_ratio,1) - 1;
                            %                                     d0 = max(dij(idf1:max(idf0,idf2),h));
                            %                                     if ~sets(l).mgs(1) && sets(l).mgs(2) %max(sets(l).mgs(2:3))
                            %                                         sets(l).mgs(1) = d0;
                            %                                     end
                            %                                     sets(l).globmg = globmg(sets(l).mgs) * d0;
                            l = l + 1;
                        else
                            d0 = zeros(1,length(sets(l).mgs));
                            if find(sets(l).mgs,1) > 3
                                sets(l) = [];
                                continue
                            end
                            for p = 1:min(2,length(sets(l).mgs))
                                if p == 1 || ~sets(l).mgs(p)
                                    idf0 = find(fj(:,h) >= sets(l).meanf0 * p,1);
                                    %                                             idf1 = find(fj(:,h) >= sets(l).meanf0 * p / quartertone_ratio,1);
                                    %                                             idf2 = find(fj(:,h) >= sets(l).meanf0 * p * quartertone_ratio,1) - 1;
                                    %                                             d0(p) = max(dij(idf1:max(idf0,idf2),h));
                                    d0(p) = dij(idf0,h);
                                    if ~sets(l).mgs(p) && d0(p) > max(sets(l).mgs) / 5
                                        sets(l).mgs(p) = d0(p);
                                    end
                                end
                            end
                            sets(l).globmg = globmg(sets(l).mgs,sets(l).pidx) * d0(1);
                            
                            if ~transfered && sets(l).globmg > .5 && sets(l).mgs(1)
                                integrate = 1;
                                if ~isempty(finalset)
                                    for p = 1:min(length(finalset(end).fidx),length(sets(l).fidx))
                                        if ...finalset(end).fidx(p) && ...
                                                ...sets(l).fidx(p) && ...
                                                finalset(end).fidx(p) ~= sets(l).fidx(p)
                                            for q = p+1:min(length(finalset(end).fidx),length(sets(l).fidx))
                                                if ~finalset(end).fidx(q) && ~sets(l).fidx(q)
                                                    continue
                                                end
                                                if finalset(end).fidx(q) == sets(l).fidx(q)
                                                    if finalset(end).globmg < sets(l).globmg
                                                        for r = 1:length(finalset(end).pidx)
                                                            pidxr = finalset(end).pidx(r);
                                                            if pidxr
                                                                isharmonic{pidxr}(end) = [];
                                                            end
                                                        end
                                                        finalset(end) = [];
                                                        integrate = 1;
                                                    else
                                                        integrate = 0;
                                                    end
                                                    break
                                                end
                                            end
                                            break
                                        end
                                    end
                                end
                                if integrate
                                    finalset(end+1) = sets(l);
                                    finalset(end).sub = [];
                                    transfered = true;
                                    for p = 1:length(sets(l).pidx)
                                        pidxp = sets(l).pidx(p);
                                        if ~pidxp
                                            continue
                                        end
                                        for q = 1:length(isharmonic{pidxp})
                                            if isharmonic{pidxp}(q).index == p || mod(isharmonic{pidxp}(q).index,p)
                                                continue
                                            end
                                            finalset(end).sub(end+1) = isharmonic{pidxp}(q).finalset;
                                        end
                                        isharmonic{pidxp}(end+1).index = p;
                                        isharmonic{pidxp}(end).finalset = length(finalset);
                                    end
                                end
                            end
                            sets(l) = [];
                        end
                    end
                    sets(end+1).f0s = [f0k,zeros(1,9)];
                    sets(end).fs = [fj(fidx,h),zeros(1,9)];
                    sets(end).harm = [idx,zeros(1,9)];
                    sets(end).nh = 1;
                    sets(end).fidx(idx) = fidx;
                    sets(end).pidx(idx) = pidx;
                    sets(end).mgs(idx) = dij(fidx,h);
                    sets(end).meanf0 = f0k;
                    %                             idf0 = find(fj(:,h) >= f0k,1);
                    %                             idf1 = find(fj(:,h) >= f0k / quartertone_ratio,1);
                    %                             idf2 = find(fj(:,h) >= f0k * quartertone_ratio,1) - 1;
                    %                             d0 = max(dij(idf1:max(idf0,idf2),h));
                    %                             if idx == 1
                    %                                 sets(end).globmg = globmg(sets(end).mgs) * d0;
                    %                             elseif idx == 2 %<= 3
                    %                                 sets(end).mgs(1) = d0;
                    %                                 sets(end).globmg = globmg(sets(end).mgs) * d0;
                    %                             else
                    %                                 sets(end).globmg = 0;
                    %                             end
                    
                    if pidx == length(ph)
                        count(idx) = [];
                        fk2f0s(idx) = [];
                    else
                        pidx = pidx + 1;
                        count(idx) = pidx;
                        fk2f0s(idx) = fj(ph(pidx))/idx/sqrt(1+(idx-1).^2*beta);
                    end
                end
                
%                 [unused,idx] = sort([finalset.globmg],'descend');
%                 ll = min(10,length(idx));
%                 res(1:ll,h) = [finalset(idx(1:ll)).meanf0]';
               
                
%                 for k = 1:length(pf{i}{j}{h})
%                     fk = pf{i}{j}{h}(k);
%                     mk = pa{i}{j}{h}(k);
%                     if fk < option.mi || fk > option.ma
%                         continue
%                     end
%                     
%                     if isempty(current)
%                         current.f = fk;
%                         current.m = mk;
%                         current.m2 = .2 * mk^2;
%                         current.sub = [];
%                         current.f0 = [];
%                     else
%                         sub = [];
%                         candidates = [];
%                         for l = 1:length(current)
%                             d = fk / current(l).f(1);
%                             rd = round(d);
%                             delta = abs(d-rd);
%                             if rd ~= 1 && delta < .1% && ...
% %                                     (length(current(l).delta) < rd || ...
% %                                      current(l).m(rd) == 0 || ...
% %                                      delta < current(l).delta(rd))
% %                                     (length(current(l).m) < rd || ...
% %                                     d > current(l).m(rd))
%                                 if l < length(current)
%                                     nexd = fk / current(l+1).f(1);
%                                     nexrd = round(nexd);
%                                     if nexrd == rd && ...
%                                             abs(nexd-nexrd) < delta
%                                         continue
%                                     end
%                                 end
%                                 if l > 1
%                                     pred = fk / current(l-1).f(1);
%                                     prerd = round(pred);
%                                     if prerd == rd && ...
%                                             abs(pred-prerd) < delta
%                                         continue
%                                     end
%                                 end
%                                 if k < length(pf{i}{j}{h})
%                                     nexd = pf{i}{j}{h}(k+1) / current(l).f(1);
%                                     nexrd = round(nexd);
%                                     nexdel = abs(nexd-nexrd);
%                                     nexm = pa{i}{j}{h}(k+1);
%                                     if nexrd == rd && nexdel < .1 && ...
%                                             nexdel * mk < delta * nexm
%                                         continue
%                                     end
%                                 end
%                                 if k > 1
%                                     pred = pf{i}{j}{h}(k-1) / current(l).f(1);
%                                     prerd = round(pred);
%                                     predel = abs(pred-prerd);
%                                     prem = pa{i}{j}{h}(k-1);
%                                     if prerd == rd && predel < .1 && ...
%                                             predel * mk < delta * prem
%                                         continue
%                                     end
%                                 end
%                                 current(l).f(rd) = fk;
%                                 current(l).m(rd) = mk;
%                                 if length(current(l).m) < rd - 1
%                                     current(l).m(rd - 1) = 0;
%                                 end
%                                 if mod(rd,2)
%                                     current(l).m2(rd) = ...
%                                         mk * current(l).m(rd-2) + ...
%                                         mk * current(l).m(rd-1);
%                                 else
%                                     current(l).m2(rd) = mk * ...
%                                         current(l).m(rd-1);
%                                 end
%                                 sub(end+1) = l;
%                                 candidates(end+1) = rd;
%                             end
%                         end
%                         current(end+1).f = fk;
%                         current(end).m = mk;
%                         current(end).m2 = .2 * mk^2; %max(0,mk-below)^2;
%                         current(end).sub = sub;
%                         current(end).f0 = [];
%                     end
%                 end
                
                for k = 1:length(finalset)
                    if ~isempty(finalset(k).sub)
                        continue
                    end
                    mg = finalset(k).globmg;
                    found = [];
                    if ~isempty(f0s)
                        d = finalset(k).meanf0 ./ [f0s.fnow];
                        delta = abs(log2(d));
                        for l = 1:length(f0s)
%                             if h - f0s(l).h > 1
%                                 continue
%                             end
%                             d = current(k).f(1) / f0s(l).f(end);
%                             delta = abs(log2(d));
                            if delta(l) < quartertone && ...
                                    (f0s(l).h < h || mg > f0s(l).m(h))
                                if isempty(found)
                                    found.h = f0s(l).h;
                                    found.l = l;
                                    found.mg = mg;
                                    found.delta = delta(l);
                                else
                                    found(end+1).h = f0s(l).h;
                                    found(end).l = l; 
                                    found(end).mg = mg;
                                    found(end).delta = delta(l);
                                end
                            end
                        end
                        if isempty(found)
                            d = finalset(k).meanf0 / f0s(previous).fnow;
                            delta = abs(log2(d));
                            if delta < quartertone
                                continue
                            end
                        end
                    end
                    if isempty(found)
                        f0s(end+1).f(h) = finalset(k).meanf0;
                        f0s(end).fnow = finalset(k).meanf0;
                        f0s(end).m(h) = mg;
                        f0s(end).m0(h) = NaN; %current(k).m(1);
                        f0s(end).ms(:,h) = NaN; %current(k).m';
                        f0s(end).h = h;
                        f0s(end).on = 0;
                        f0s(end).amp = 0;
%                         f0s(end).off = 0;
%                         if isempty(current(k).sub)
%                             f0s(end).sub = [];
%                         else
%                             f0s(end).sub = current(current(k).sub).f0;
%                         end
%                         f0s{end}.delta = delta;
                        finalset(k).f0 = length(f0s);
                        previous = length(f0s);
                    else
                        [delta,mh] = min([found.delta]);
                        ml = found(mh).l;
                        if length(f0s(ml).f) < h
                            if k < length(finalset)
                                nexd = finalset(k+1).meanf0 / f0s(ml).f(end);
                                nexdelta = abs(log2(nexd));
                                if nexdelta < delta
                                    continue
                                end
                            end
                            f0s(ml).f(h) = finalset(k).meanf0;
                            f0s(ml).fnow = finalset(k).meanf0;
                            f0s(ml).m(h) = mg;
                            f0s(ml).m0(h) = NaN; %current(k).m(1);
                            %%f0s(ml).ms(1:length(current(k).m),h) = current(k).m';
                            f0s(ml).h = h;
%                             for l = 1:length(current(k).sub)
%                                 if ~ismember(current(current(k).sub(l)).f0,f0s(ml).sub)
%                                     f0s(ml).sub(end+1) = current(current(k).sub(l)).f0;
%                                 end
%                             end
                            %                         f0s{ml}.delta = delta;
                            finalset(k).f0 = ml;
                            previous = ml;
                        end
                    end
                end
                                
                deltathres = .01 * sumh; %.2
                mthres = .1 * sumh;  %.2
                m0thres = .005 * sumh;
                for k = 1:length(f0s)
                    if f0s(k).h == h && f0s(k).f(end) < 1500 
                        delta = 0; %f0s(k).delta(h);
%                         sm = f0s{k}.m(h);
                        if (~f0s(k).amp) && ... || sum(f0s(k).ms(:,end)) / f0s(k).amp > 3) && ...
                                delta > deltathres && ...
                                f0s(k).m(h) > mthres && ...
                                f0s(k).m0(h) > m0thres
                            found = 0;
                            if ~isempty(notes) && ...
                                    notes(end).h < h && ...
                                    notes(end).h > h - 5 && ...
                                    ~isempty(active) && ...
                                    active(end) == length(notes) && ...
                                    ~isempty(f0s(notes(end).f0).sub) && ...
                                    ismember(k,f0s(notes(end).f0).sub) && ...
                                    (length(f0s(notes(end).f0).m) < h || ...
                                     f0s(notes(end).f0).m(h) < f0s(k).m(h))
                                notes(end) = [];
                                if ~isempty(active) && ...
                                        active(end) == length(notes) + 1
                                    active(end) = [];
                                end
                                nmat(end,:) = [];
                            end
                            f0s(k).amp = sum(f0s(k).ms(:,end));
                            for l = 1:length(f0s(k).sub)
                                f0 = f0s(k).sub(l); %current(f0s{k}.sub(l)).f0;
%                                 if f0s(f0).on
%                                     found = 1;
%                                     break
%                                 end
                                if length(f0s(f0).delta) == h && f0s(f0).delta(h) > delta / 10
                                    %f0s{f0}.m(end) > f0s{k}.m(end) %&& ...
                                    %                                             f0s{f0}.delta > f0s{k}.delta
                                    %f0s{f0}.m(end) - min(f0s{f0}.m(end-5:end-1)) > ...
                                    %f0s{k}.m(end) - min(f0s{k}.m(end-5:end-1))
                                    %%f0s{f0}.m(end) > f0s{k}.m(end) * 5)
                                    %                                         r = round(f0s{k}.f(end)/f0s{f0}.f(end));
                                    %                                         m0 = current(f0s{f0}.k).m;
                                    %                                         if log2(m0(2*r)/m0(2*r-1)) > .3
                                    found = 1;
                                    break
                                end
                            end
                            if ~found
                                f0s(k).on = 1;
%                                 if ~f0s(k).off || h - f0s(k).off > 20
                                    pit = 60 + round(12 * log2(f0s(k).fnow / 261.626));
                                    if ~isempty(active)
                                        prev = find([notes(active).pit] == pit,1);
                                        if ~isempty(prev)
                                            dur = fp{i}{j}(1,h) - nmat(active(prev),1);
                                            nmat(active(prev),[2 7]) = dur;
                                            activeprev = active(prev);
                                            active(prev) = [];
                                            if h - notes(activeprev).h < 3
%                                                 notes(activeprev) = [];
%                                                 nmat(activeprev,:) = [];
%                                                 active(prev:end) = active(prev:end) - 1;
                                            end
                                        end
                                    end
                                    if isempty(notes)
                                        notes(1).h = h;
                                    else
                                        notes(end+1).h = h;
                                    end
                                    active(end+1) = length(notes);
                                    notes(end).f0 = k;
                                    f0s(k).fnote = f0s(k).fnow;
                                    if isempty(f0s(k).sub)
                                        notes(end).sub = [];
                                    else
                                        notes(end).sub = f0s(k).sub;
                                    end
                                    tim = fp{i}{j}(1,h);
                                    notes(end).pit = pit;
                                    vel = 100;
                                    nmat(end+1,:) = [tim 0 1 pit vel tim 0];
                                    %                                     indices(end+1) = current(f0s{k}.k).k;
%                                 end
                            end
                        elseif f0s(k).on 
                            if h > 1 && f0s(k).m(h) < mthres && diff(f0s(k).m(h-1:h)) <= 0
%                                 f0s(k).off = h;
                                f0s(k).on = 0;
                                f0s(k).amp = 0;
                                for l = 1:length(active)
                                    if notes(active(l)).f0 == k
                                        dur = fp{i}{j}(1,h) - nmat(active(l),1);
                                        nmat(active(l),[2 7]) = dur;
                                        activeprev = active(l);
                                        active(l) = [];
                                        if h - notes(activeprev).h < 3
                                            %                                         notes(activeprev) = [];
                                            %                                         nmat(activeprev,:) = [];
                                            %                                         active(l:end) = active(l:end) - 1;
                                        end
                                        break
                                    end
                                end
                            else
                                d = f0s(k).fnote / f0s(k).fnow;
                                delta = abs(log2(d));
                                if delta >= 1/12
                                    for l = 1:length(active)
                                        if notes(active(l)).f0 == k
                                            dur = fp{i}{j}(1,h) - nmat(active(l),1);
                                            nmat(active(l),[2 7]) = dur;
                                            break
                                        end
                                    end
                                    pit = 60 + round(12 * log2(f0s(k).fnow / 261.626));
                                    notes(end+1).h = h;
                                    active(end+1) = length(notes);
                                    notes(end).f0 = k;
                                    f0s(k).fnote = f0s(k).fnow;
                                    if isempty(f0s(k).sub)
                                        notes(end).sub = [];
                                    else
                                        notes(end).sub = f0s(k).sub;
                                    end
                                    tim = fp{i}{j}(1,h);
                                    notes(end).pit = pit;
                                    vel = 100;
                                    nmat(end+1,:) = [tim 0 1 pit vel tim 0];
                                end
                            end
                        elseif f0s(k).amp && ...
                                h > 1 && ...
                                f0s(k).m(h) < mthres && diff(f0s(k).m(h-1:h)) <= 0
                            f0s(k).amp = 0;
                        end
                       
%                         if h == 1
%                             dm = f0s{ml}.m;
%                         else
%                             dm = diff(f0s{ml}.m(h-1:h));
%                         end
%                         if f0s{ml}.on <= 0 && ...dm > 1000 && ...
%                             ...(f0s{ml}.on == 0 || (
%                             dm > .005 && sm > thres %.02, .05
%                         if f0s{ml}.on < 1 
%                             if sm > max(f0s{ml}.min * 2, thres)
%                                 f0s{ml}.on = 1;
%                                 indices(end+1) = current(k).k;
%                             elseif sm < f0s{ml}.min
%                                 f0s{ml}.min = sm;
%                             end
%                         elseif dm < 0 && sm < thres %*10 %&& dm < -.005
%                             f0s{ml}.on = -1;
%                             f0s{end}.min = sm;
%                         end

                        
                    end
                end
                
%                 if isempty(current)
%                     indices = [];
%                 elseif length(current) == 1
%                     indices = current.k;
%                 else
%                     sm = zeros(1,length(current));
%                     for k = 1:length(current)
%                         sm(k) = sum(current(k).m2);
%                     end
%                     [unused,order] = sort(sm);
%                     indices = [current(order(end)).k,...
%                                current(order(end-1)).k];
% %                     indices = current(order(end)).k;
%                 end
            end
            
            k = 1;
            while k <= length(f0s)
                if length(find(f0s(k).f)) < 5
                    f0s(k) = [];
                else
                    k = k + 1;
                end
            end
            
            figure, hold on
            for k = 1:length(f0s)
                plot(f0s(k).f,'+')
            end
            
            figure, hold on
            for k = 1:length(f0s)
                plot(log10(f0s(k).m))
            end
            
            figure,imagesc(m);
            set(gca,'YDir','normal')

            h = 1;
            k = 1;
            writemidi(nmat,'transcription.mid');
            while k <= length(notes)
                while h < notes(k).h
                    pf{i}{j}{h} = [];
                    pa{i}{j}{h} = [];
                    pp{i}{j}{h} = [];
                    pv{i}{j}{h} = [];
                    pm{i}{j}{h} = [];
                    h = h+1;
                end
                indices = [];
                pf{i}{j}{h} = pf{i}{j}{h}(indices);
                pa{i}{j}{h} = pa{i}{j}{h}(indices);
                pp{i}{j}{h} = pp{i}{j}{h}(indices);
                pv{i}{j}{h} = pv{i}{j}{h}(indices);
                pm{i}{j}{h} = pm{i}{j}{h}(indices);
                k = k+1;
                h = h+1;
            end
            while h <= length(pf{i}{j})
                pf{i}{j}{h} = [];
                pa{i}{j}{h} = [];
                pp{i}{j}{h} = [];
                pv{i}{j}{h} = [];
                pm{i}{j}{h} = [];
                h = h+1;
            end
        end
    end
%     figure, hold on
%     for i = 1:length(f0s)
%         if 1 %abs(f0s{i}.on)
%             plot(f0s{i}.m)
%         end
%     end
%     figure, hold on
%     for i = 1:length(f0s)
%         if 1 %abs(f0s{i}.on)
%             plot(f0s{i}.f)
%         end
%     end
%     figure, hold on
%     for i = 1:length(f0s)
%         if 1 %abs(f0s{i}.on)
%             plot(diff(f0s{i}.m))
%         end
%     end
%     figure(51),clf,hold on
%     figure(52),clf,hold on
%     figure(53),clf,hold on
%     for i = 1:length(f0s)
%         if abs(f0s{i}.on)
%             figure(51),plot(f0s{i}.m)
%             figure(52),plot(f0s{i}.f)
%             figure(53),plot(diff(f0s{i}.m))
% %             pause
%         end
%     end
    x = set(x,'PeakPrecisePos',pf,'PeakPreciseVal',pa,...
              'PeakPos',pp,'PeakVal',pv,'PeakMode',pm);
end

if (option.cent || strcmpi(option.segm,'Lartillot') || strcmpi(option.segm,'Lartillot2')) && ...
        (~isa(x,'mirpitch') || strcmp(get(x,'Unit'),'Hz'))
    punit = 'cents';
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            for k = 1:size(pf{i}{j},3)
                for l = 1:size(pf{i}{j},2)    
                    pf{i}{j}{1,l,k} = 1200*log2(pf{i}{j}{1,l,k});
                end
            end
        end
    end
end

startp = [];
if strcmpi(option.segm,'Lartillot')
    scale = [];
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            if size(pf{i}{j},2) == 1 && size(pf{i}{j}{1},2) > 1
                pfj = cell(1,size(pf{i}{j}{1},2));
                paj = cell(1,size(pa{i}{j}{1},2));
                for l = 1:size(pf{i}{j}{1},2)
                    if isnan(pf{i}{j}{1}(l))
                        pfj{l} = [];
                        paj{l} = 0;
                    else
                        pfj{l} = pf{i}{j}{1}(l);
                        paj{l} = pa{i}{j}{1}(l);
                    end
                end
                pf{i}{j} = pfj;
                pa{i}{j} = paj;
            end
            
            for k = 1:size(pf{i}{j},3)
                startp = [];
                meanp = [];
                endp = [];
                deg = [];
                stabl = [];
                buffer = [];
                breaks = [];
                currentp = [];
                maxp = 0;
                reson = [];
                attack = [];
                
                if ~isempty(pf{i}{j}{1,1,k})
                    pf{i}{j}{1,1,k} = pf{i}{j}{1,1,k}(1);
                    pa{i}{j}{1,1,k} = pa{i}{j}{1,1,k}(1);
                end
                if ~isempty(pf{i}{j}{1,end,k})
                    pf{i}{j}{1,end,k} = pf{i}{j}{1,end,k}(1);
                    pa{i}{j}{1,end,k} = pa{i}{j}{1,end,k}(1);
                end
                
                for l = 2:size(pf{i}{j},2)-1
                    if ~isempty(pa{i}{j}{1,l,k}) && ...
                            pa{i}{j}{1,l,k}(1) > maxp
                        maxp = pa{i}{j}{1,l,k}(1);
                    end
                    if ~isempty(reson) && l-reson(1).end>50
                        reson(1) = [];
                    end
                    
                    if ~isempty(pf{i}{j}{1,l,k})
                        if 1 %isempty(pf{i}{j}{1,l-1,k})
                            pf{i}{j}{1,l,k} = pf{i}{j}{1,l,k}(1);
                            pa{i}{j}{1,l,k} = pa{i}{j}{1,l,k}(1);                            
                        else
                            [dpf idx] = min(abs(pf{i}{j}{1,l,k} - ...
                                                pf{i}{j}{1,l-1,k}));
                            if idx > 1 && ...
                                    pa{i}{j}{1,l,k}(1) - pa{i}{j}{1,l,k}(idx) > .02
                                pf{i}{j}{1,l,k} = pf{i}{j}{1,l,k}(1);
                                pa{i}{j}{1,l,k} = pa{i}{j}{1,l,k}(1);
                            else
                                pf{i}{j}{1,l,k} = pf{i}{j}{1,l,k}(idx);
                                pa{i}{j}{1,l,k} = pa{i}{j}{1,l,k}(idx);
                            end
                        end
                    end
                    
                    interrupt = 0;
                    if l == size(pf{i}{j},2)-1 || ...
                            isempty(pf{i}{j}{1,l,k}) || ...
                            (~isempty(buffer) && ...
                             abs(pf{i}{j}{1,l,k} - pf{i}{j}{1,l-1,k})...
                                > option.segpitch) || ...
                            (~isempty(currentp) && ...
                             abs(pf{i}{j}{1,l,k} - currentp) > ...
                                option.segpitch)
                        interrupt = 1;
                    elseif (~isempty(pa{i}{j}{1,l-1,k}) && ...
                             pa{i}{j}{1,l,k} - pa{i}{j}{1,l-1,k} > .01)
                        interrupt = 2;
                    end
                    
                    if ~interrupt
                        for h = 1:length(reson)
                            if abs(pf{i}{j}{1,l,k}-reson(h).pitch) < 50 && ...
                                    pa{i}{j}{1,l,k} < reson(h).amp/5
                                pa{i}{j}{1,l,k} = [];
                                pf{i}{j}{1,l,k} = [];
                                interrupt = 1;
                                break
                            end
                        end
                    end
                    
                    if interrupt
                        % Segment interrupted
                        if isempty(buffer) || ...
                                ...%length(buffer.pitch) < option.segmin || ...
                                0 %std(buffer.pitch) > 25
                             if length(startp) > length(endp)
                                startp(end) = [];
                            end
                        else
                            if isempty(currentp)
                                strong = find(buffer.amp > max(buffer.amp)*.75);
                                meanp(end+1) = mean(buffer.pitch(strong));
                            else
                                meanp(end+1) = currentp;
                            end
                            endp(end+1) = l-1;
                            hp = hist(buffer.pitch,5);
                            hp = hp/sum(hp);
                            entrp = -sum(hp.*log(hp+1e-12))./log(length(hp));
                            stabl(end+1) = entrp>.7;                        
                            deg(end+1) = cent2deg(meanp(end),scale);
                            reson(end+1).pitch = meanp(end);
                            reson(end).amp = mean(buffer.amp);
                            reson(end).end = l-1;
                            attack(end+1) = max(buffer.amp) > .05;
                        end
                              
                        if isempty(pf{i}{j}{1,l,k})
                            buffer = [];
                        else
                            buffer.pitch = pf{i}{j}{1,l,k};
                            buffer.amp = pa{i}{j}{1,l,k};
                            startp(end+1) = l;
                        end
                        currentp = [];
                        breaks(end+1) = l;
                        
                    elseif isempty(buffer)
                        % New segment starting
                        startp(end+1) = l;
                        buffer.pitch = pf{i}{j}{1,l,k};
                        buffer.amp = pa{i}{j}{1,l,k};
                        
                    else
                        if length(pf{i}{j}{1,l,k})>1
                            mirerror('mirpitch','''Segment'' option only for monodies (use also ''Mono'')');
                        end
                        buffer.pitch(end+1) = pf{i}{j}{1,l,k};
                        buffer.amp(end+1) = pa{i}{j}{1,l,k};
                        if length(buffer.pitch) > 4 && ...
                                std(buffer.pitch(1:end)) < 5 && ...
                                buffer.amp(end) > max(buffer.amp)*.5
                            currentp = mean(buffer.pitch(1:end));
                        %else
                        %    l
                        end
                    end                    
                end
                
                if length(startp) > length(meanp)
                    startp(end) = [];
                end
                
                l = 1;
                while l <= length(endp)
                    if 1 %~isempty(intersect(startp(l)-(1:5),breaks)) && ...
                         %  ~isempty(intersect(endp(l)+(1:5),breaks))
                        if 1 %attack(l)
                            minlength = option.segmin;
                        else
                            minlength = 6;
                        end
                    else
                        minlength = 2;
                    end
                    if endp(l)-startp(l) > minlength
                    % Segment sufficiently long
                        if l>1 && ~attack(l) && ...
                           startp(l) <= endp(l-1)+option.segtime && ...
                            abs(meanp(l)-meanp(l-1)) < 50
                                % Segment fused with previous one
                                startp(l) = [];
                                %meanp(l-1) = mean(meanp(l-1:l));
                                meanp(l) = [];
                                deg(l-1) = cent2deg(meanp(l-1),scale);
                                deg(l) = [];
                                attack(l-1) = max(attack(l),attack(l-1));
                                attack(l) = [];
                                endp(l-1) = [];
                                found = 1;
                        else
                                l = l+1;
                        end
                    % Other cases: Segment too short
                    elseif l>1 && ...
                            startp(l) <= endp(l-1)+option.segtime && ...
                            abs(meanp(l)-meanp(l-1)) < 50
                        % Segment fused with previous one
                        startp(l) = [];
                        %meanp(l-1) = mean(meanp(l-1:l));
                        meanp(l) = [];
                        deg(l) = [];
                        attack(l-1) = max(attack(l),attack(l-1));
                        attack(l) = [];
                        endp(l-1) = [];
                    elseif 0 && l < length(meanp) && ...
                            startp(l+1) <= endp(l)+option.segtime && ...
                            abs(meanp(l+1)-meanp(l)) < 50
                        % Segment fused with next one
                        startp(l+1) = [];
                        meanp(l) = meanp(l+1); %mean(meanp(l:l+1));
                        meanp(l+1) = [];
                        deg(l) = deg(l+1);
                        deg(l+1) = [];
                        attack(l) = max(attack(l),attack(l+1));
                        attack(l+1) = [];
                        endp(l) = [];
                    else
                        % Segment removed
                        startp(l) = [];
                        meanp(l) = [];
                        deg(l) = [];
                        attack(l) = [];
                        endp(l) = [];
                    end
                end               
                               
                l = 1;
                while l <= length(endp)
                    if (max([pa{i}{j}{1,startp(l):endp(l),k}]) < maxp/20 ...
                                && isempty(pa{i}{j}{1,startp(l)-1,k}) ...
                                && isempty(pa{i}{j}{1,endp(l)+1,k})) ...
                            || endp(l) - startp(l) < option.segmin
                        % Segment removed
                        fusetest = endp(l) - startp(l) < option.segmin;                        
                        startp(l) = [];
                        meanp(l) = [];
                        deg(l) = [];
                        endp(l) = [];
                        stabl(l) = [];
                        attack(l) = [];
                        
                        if fusetest && ...
                                l > 1 && l <= length(meanp) && ...
                                abs(meanp(l-1)-meanp(l)) < 50
                            % Preceding segment fused with next one
                            startp(l) = [];
                            meanp(l-1) = meanp(l); %mean(meanp(l:l+1));
                            meanp(l) = [];
                            deg(l-1) = deg(l);
                            deg(l) = [];
                            attack(l-1) = max(attack(l),attack(l-1));
                            attack(l) = [];
                            endp(l-1) = [];
                        end
                    else
                        l = l+1;
                    end
                end

                if option.octgap
                    l = 2;
                    while l <= length(endp)
                        if abs(meanp(l-1) - meanp(l) - 1200) < 50
                            % Segment removed
                            startp(l) = [];
                            meanp(l-1) = meanp(l);
                            meanp(l) = [];
                            deg(l-1) = deg(l);
                            deg(l) = [];
                            attack(l) = [];
                            endp(l-1) = [];
                            stabl(l) = [];
                        elseif abs(meanp(l) - meanp(l-1) - 1200) < 50
                            % Segment removed
                            startp(l) = [];
                            meanp(l) = meanp(l-1);
                            meanp(l) = [];
                            deg(l) = deg(l-1);
                            deg(l) = [];
                            attack(l) = [];
                            endp(l-1) = [];
                            stabl(l) = [];
                        else
                            l = l+1;
                        end
                    end
                end
                
                ps{i}{j}{k} = startp;
                pe{i}{j}{k} = endp;
                pm{i}{j}{k} = meanp;
                stb{i}{j}{k} = stabl;
                dg = {}; %{i}{j}{k} = deg;
            end
        end
    end
elseif strcmpi(option.segm,'Lartillot2')   %%%%% NEW
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            for g = 1:size(pf{i}{j},3)
                pitches = zeros(1,length(pf{i}{j}));
                for k = 1:length(pf{i}{j})
                    pfk = pf{i}{j}{k};
                    if isempty(pfk)
                        pitches(k) = NaN;
                    else
                        pitches(k) = pfk;
                    end
                end
                
                figure, hold on
                plot(pitches,'+')
                
                meanp = [];
                startp = [];
                endp = [];
                stable = [];
                oldstable = [];
                for k = 2:length(pitches)
                    if isnan(pitches(k))
                        if ~isempty(stable)
                            oldstable = stable;
                        end
                        stable = [];
                        continue
                    end
                    if isnan(pitches(k-1))
                        stable = [];
                        continue
                    end
                    dp = pitches(k) - pitches(k-1);
                    adp = abs(dp);
                    if adp > 15
                        if ~isempty(stable)
                            oldstable = stable;
                        end
                        stable = [];
                        continue
                    end
                    if adp < 10
                        if isempty(stable)
                            stable.p = pitches(k);
                            stable.dp = adp;
                            stable.idx = k;
                            if k == 2 || isnan(pitches(k-2))
                                stable.pre = 0;
                            else
                                stable.pre = sign(pitches(k-1) - pitches(k-2));
                            end
                            if ~isempty(oldstable) && ...
                                    oldstable.pre * stable.pre == -1
                                meanp(end+1) = mean([oldstable.p,stable.p]);
                                startp(end+1) = oldstable.idx;
                            end
                        elseif adp < stable.dp
                            stable.p = pitches(k);
                            stable.dp = adp;
                            stable.idx = k;
                        end
                    end
                end
                
                ps{i}{j}{g} = startp;
                pe{i}{j}{g} = endp;
                pm{i}{j}{g} = meanp;
                stb{i}{j}{g} = [];
                dg = {}; 
            end
        end
    end
elseif strcmpi(option.segm,'Nymoen')
    fundamental = 1;
    medianFilterLength1 = 15;
    derivTreshold1 = 1;
    medianFilterLength2 = 21;
    derivTreshold2 = 5;
    toneLengthTreshold = 25; %10;
    rmsThreshold = 0.005;
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            for g = 1:size(pf{i}{j},3)
                pitches = zeros(1,length(pf{i}{j}));
                for k = 1:length(pf{i}{j})
                    pfk = pf{i}{j}{k};
                    if isempty(pfk)
                        pitches(k) = NaN;
                    else
                        pitches(k) = pfk;
                    end
                end
                
%                 figure, hold on
%                 plot(pitches,'+')
                smoothpitches = medfilt1(pitches,medianFilterLength1,'omitnan','truncate');
%                 plot(smoothpitches,'+')
                smoothpitches(isnan(pitches)) = nan;
%                 plot(smoothpitches,'+')

%               Variant, by Olivier
%                 smoothpitches = NaN(1,length(pitches));
%                 buff = [];
%                 for k = 1:length(pitches)
%                     if isnan(pitches(k))
%                         buff = [];
%                         continue
%                     end
%                     if length(buff) == medianFilterLength1
%                         buff(1) = [];
%                     end
%                     buff(end+1) = pitches(k);
%                     if length(buff) == medianFilterLength1
%                         smoothpitches(k) = mean(buff);
%                     end
%                 end
                
                %Derivate the pitch curve. If the pitch derivative is larger than
                %threshold, remove pitch information (in effect: segment curve)
                smoothderiv = [0 diff(smoothpitches)];
                smoothpitches(abs(smoothderiv)>derivTreshold1) = nan;
%                 plot(smoothpitches,'o')
                
                %If the RMS energy is lower than threshold, remove pitch information
%                 smoothpitches(rmss < rmsThreshold) = nan;
                
                %Another round of filtering, derivation and thresholding.
                smoothpitches = medfilt1(smoothpitches,medianFilterLength2,'omitnan','truncate');
%                 smoothpitches(rmss < rmsThreshold) = nan;
                smoothderiv = [0 diff(smoothpitches)];
                smoothpitches(abs(smoothderiv)>derivTreshold2) = nan;
%                 plot(smoothpitches,'x')
                
                h = 1;
                l = [0 0];
                startp = [];
                endp = [];
                stabl = [];
                
                tones = {};
                startp = [];
                endp = [];
                %Identify tones.
                % If the pitch curve is nan, and the following non-nan period is longer
                % than toneLengthThreshold, then identify a new tone.
                for k = 1:length(smoothpitches)
                    if isnan(smoothpitches(k))
                        if l(2) > 0 && l(1)-l(2) > toneLengthTreshold
                            tones{h} = smoothpitches(l(2)+1:l(1));
                            startp(h) = l(2);
                            endp(h) = l(1);
                            h = h+1;
                        end
                        l(2) = k;
                    end
                    l(1) = k;
                end
                
                %pitch of each tone is the median of all underlying pitches
                meanp = zeros(1,length(tones));
                for k = 1:length(tones)
                    meanp(k) = median(tones{k})/fundamental;
                end
                ps{i}{j}{g} = startp;
                pe{i}{j}{g} = endp;
                pm{i}{j}{g} = meanp;
                stb{i}{j}{g} = stabl;
                dg = {}; 
            end
        end
    end
elseif isa(x,'mirpitch')
    ps = get(x,'Start');
    pe = get(x,'End');
    pm = get(x,'Mean');
    dg = get(x,'Degrees');
    stb = get(x,'Stable');
elseif isa(x,'mirmidi')
    nm = get(x,'Data');
    for i = 1:length(nm)
        startp = nm{i}(:,1);
        endp = startp + nm{i}(:,2);
        fp{i} = [startp endp]';
        ps{i} = {{1:length(startp)}};
        pe{i} = {{1:length(endp)}};
        pm{i} = {{nm{i}(:,4)'-68}};
        dg{i} = pm{i};
        stb{i} = [];
        pf{i} = {NaN(size(startp'))};
    end
    x = set(x,'FramePos',{fp});
else
    ps = {};
    pe = {};
    pm = {};
    dg = {};
    stb = {};
end

if option.stable(1) < Inf
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            for k = 1:size(pf{i}{j},3)
                for l = size(pf{i}{j},2):-1:option.stable(2)+1
                    for m = length(pf{i}{j}{1,l,k}):-1:1
                        found = 0;
                        for h = 1:option.stable(2)
                            for n = 1:length(pf{i}{j}{1,l-h,k})
                                if abs(log10(pf{i}{j}{1,l,k}(m) ...
                                            /pf{i}{j}{1,l-h,k}(n))) ...
                                       < option.stable(1)
                                    found = 1;
                                end
                            end
                        end
                        if not(found)
                            pf{i}{j}{1,l,k}(m) = [];
                        end
                    end
                    pf{i}{j}{1,1,k} = zeros(1,0);
                end
            end
        end
    end
end
if option.median
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            if size(fp{i}{j},2) > 1
                npf = zeros(size(pf{i}{j}));
                for k = 1:size(pf{i}{j},3)
                    for l = 1:size(pf{i}{j},2)
                        if isempty(pf{i}{j}{1,l,k})
                            npf(1,l,k) = NaN;
                        else
                            npf(1,l,k) = pf{i}{j}{1,l,k}(1);
                        end
                    end
                end
                pf{i}{j} = medfilt1(npf,...
                     round(option.median/(fp{i}{j}(1,2)-fp{i}{j}(1,1))));
            end
        end
    end
end
if option.nooctave
    for i = 1:length(pf)
        for j = 1:length(pf{i})
            if size(fp{i}{j},2) > 1
                for k = 2:size(pf{i}{j},2)
                    if ~isempty(pf{i}{j}{1,k}) && ...
                            ~isempty(pf{i}{j}{1,k-1})
                        if pf{i}{j}{1,k-1} / pf{i}{j}{1,k} > 2.8
                            pf{i}{j}{1,k} = pf{i}{j}{1,k} * 3;
                        elseif pf{i}{j}{1,k-1} / pf{i}{j}{1,k} > 1.8
                            pf{i}{j}{1,k} = pf{i}{j}{1,k} * 2;
                        elseif pf{i}{j}{1,k} / pf{i}{j}{1,k-1} > 2.8
                            pf{i}{j}{1,k-1} = pf{i}{j}{1,k-1} * 3;
                        elseif pf{i}{j}{1,k} / pf{i}{j}{1,k-1} > 1.8
                            pf{i}{j}{1,k-1} = pf{i}{j}{1,k-1} * 2;
                        end
                    end
                end
            end
        end
    end
end
clear p
if 0 %isa(x,'mirscalar')
    p.amplitude = 0;
else
    p.amplitude = pa;
end
p.start = ps;
p.end = pe;
p.mean = pm;
p.degrees = dg;
p.stable = stb;
s = mirscalar(x,'Data',pf,'Title','Pitch','Unit',punit);
p = class(p,'mirpitch',s);
o = {p,x};

% nn = length(startp);
% nmat = zeros(nn,7);
% for i = 1:nn
%     tim = fp{1}{1}(1,startp(i));
%     dur = fp{1}{1}(2,endp(i)) - tim;
%     pit = 60 + round(12 * log2(meanp(i) / 261.626));
%     nmat(i,:) = [tim dur 1 pit 100 tim dur];
% end
% writemidi(nmat,'transcription.mid');


function [deg ref] = cent2deg(cent,ref)
deg = round((cent-ref)/100);
if isempty(deg)
    deg = 0;
end
%ref = cent - deg*100


function f0 = fk2f0(fk,k,beta)
%fk = f0*h*sqrt(1+(h^2-1)*beta)
f0 = fk./k./sqrt(1+(k-1).^2*beta);


function res = globmg(amps,pidx)
% amps,pidx
res = 0; %.2 * amps(1)^2;
for i = 2:length(amps)
%     if mod(i,2) % odd harmonics
%         res = res + max(amps(i) * amps(i-2), amps(i) * amps(i-1));
%     else        % even harmonics
    if pidx(i) && pidx(i-1)
        res = res + amps(i) * amps(i-1);
    else
        res = res + amps(i) * amps(i-1)/10;
    end
%     end
end
% res