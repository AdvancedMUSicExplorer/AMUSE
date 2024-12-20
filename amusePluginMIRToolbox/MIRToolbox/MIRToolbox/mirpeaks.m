function varargout = mirpeaks(orig,varargin)
%   p = mirpeaks(x) detect peaks in x.
%   Optional argument:
%       mirpeaks(...,'Total',m): only the m highest peaks are selected.
%           If m=Inf, no limitation of number of peaks.
%               Default value: m = Inf
%       mirpeaks(...,'Order',o): specifies the ordering of the peaks.
%           Possible values for o:
%               'Amplitude': orders the peaks from highest to lowest
%                   (Default choice.)
%               'Abscissa': orders the peaks along the abscissa axis.
%       mirpeaks(...,'Contrast',cthr): a threshold value. A given local
%           maximum will be considered as a peak if the difference of 
%           amplitude with respect to both the previous and successive 
%           local minima (when they exist) is higher than the threshold 
%           cthr. This distance is expressed with respect to the
%           total amplitude of x: a distance of 1, for instance, is
%           equivalent to the distance between the maximum and the minimum
%           of x.
%               Default value: cthr = 0.1
%           mirpeaks(...,'SelectFirst',fthr): If the 'Contrast' selection has
%               been chosen, this additional option specifies that when one
%               peak has to be chosen out of two candidates, and if the 
%               difference of their amplitude is below the threshold fthr,
%               then the most ancien one is selected.
%               Option toggled off by default:
%               Default value if toggled on: fthr = cthr/2
%       mirpeaks(...,'Threshold',thr): a threshold value.
%           A given local maximum will be considered as a peak if its
%               normalized amplitude is higher than this threshold. 
%           A given local minimum will be considered as a valley if its
%               normalized amplitude is lower than this threshold. 
%           The normalized amplitude can have value between 0 (the minimum 
%               of the signal in each frame) and 1 (the maximum in each 
%               frame).
%           Default value: thr=0 for peaks thr = 1 for valleys
%       mirpeaks(...,'Interpol',i): estimates more precisely the peak
%           position and amplitude using interpolation. Performed only on
%           data with numerical abscissae axis.
%           Possible value for i:
%               '', 'no', 'off', 0: no interpolation
%               'Quadratic': quadratic interpolation. (default value).
%       mirpeaks(...,'Valleys'): detect valleys instead of peaks.
%       mirpeaks(...,'Reso',r): removes peaks whose abscissa distance to 
%           one or several higher peaks is lower than a given threshold.
%           Possible value for the threshold r:
%               'SemiTone': ratio between the two peak positions equal to
%                   2^(1/12)
%               a numerical value : difference between the two peak
%                   positions equal to that value
%           When two peaks are distant by an interval lower than the
%               resolution, the highest of them is selected by default.
%           mirpeaks(...,'Reso',r,'First') specifies on the contrary that
%               the first of them is selected by default.
%           When a peak p1 is too close to another higher peak p2, p1 is
%               removed even if p2 is removed as well. If you want to
%               filter out p1 only if p2 remains in the end, add the option
%               'Loose'.
%           mirpeaks(...,'Reso',r,'Loose') specifies instead that 
%       mirpeaks(...,'Nearest',t,s): takes the peak nearest a given abscisse
%           values t. The distance is computed either on a linear scale
%           (s = 'Lin') or logarithmic scale (s = 'Log'). In this case,
%           only one peak is extracted.
%       mirpeaks(...,'Pref',c,std): indicates a region of preference for
%           the peak picking, centered on the abscisse value c, with a
%           standard deviation of std.
%       mirpeaks(...,'NoBegin'): does not consider the first sample as a
%           possible peak candidate.
%       mirpeaks(...,'NoEnd'): does not consider the last sample as a possible
%           peak candidate.
%       mirpeaks(...,'Normalize',n): specifies whether frames are
%           normalized globally or individually.
%           Possible value for n:
%               'Global': normalizes the whole frames altogether from 0 to 
%                   1 (default choice).
%               'Local': normalizes each frame from 0 to 1.
%       mirpeaks(...,'Extract'): extracts from the initial curves all the 
%           positive continuous segments (or "curve portions") where peaks
%           are located.
%       mirpeaks(...,'Only'): keeps from the original curve only the data
%           corresponding to the peaks, and zeroes the remaining data.
%       mirpeaks(...,'Track',t): tracks temporal continuities of peaks. If
%           a value t is specified, the variation between successive peaks
%           is tolerated up to t samples.
%       mirpeaks(...,'CollapseTrack',ct): collapses tracks into one single
%           track, and remove small track transitions, of length shorter
%           than ct samples. Default value: ct = 7

        m.key = 'Total';
        m.type = 'Integer';
        m.default = Inf;
    option.m = m;
        
        nobegin.key = 'NoBegin';
        nobegin.type = 'Boolean';
        nobegin.default = 0;
    option.nobegin = nobegin;
        
        noend.key = 'NoEnd';
        noend.type = 'Boolean';
        noend.default = 0;
    option.noend = noend;
        
        order.key = 'Order';
        order.type = 'String';
        order.choice = {'Amplitude','Abscissa'};
        order.default = 'Amplitude';
    option.order = order;
    
        chro.key = 'Chrono'; % obsolete, corresponds to 'Order','Abscissa'
        chro.type = 'Boolean';
        chro.default = 0;
    option.chro = chro;
    
        ranked.key = 'Ranked'; % obsolete, corresponds to 'Order','Amplitude'
        ranked.type = 'Boolean';
        ranked.default = 0;
    option.ranked = ranked;
        
        vall.key = 'Valleys';
        vall.type = 'Boolean';
        vall.default = 0;
    option.vall = vall;
    
        cthr.key = 'Contrast';
        cthr.type = 'Integer';
        cthr.default = .1;
    option.cthr = cthr;
    
        first.key = 'SelectFirst';
        first.type = 'Integer';
        first.default = 0;
        first.keydefault = NaN;
    option.first = first;
    
        thr.key = 'Threshold';
        thr.type = 'Integer';
        thr.default = NaN;
    option.thr = thr;
            
        smthr.key = 'MatrixThreshold'; % to be documented in version 1.3
        smthr.type = 'Integer';
        smthr.default = NaN;
    option.smthr = smthr;
    
        graph.key = 'Graph';
        graph.type = 'Integer';
        graph.default = 0;
        graph.keydefault = 1; %.25;
    option.graph = graph;
        
        interpol.key = 'Interpol';
        interpol.type = 'String';
        interpol.default = 'Quadratic';
        interpol.keydefault = 'Quadratic';
    option.interpol = interpol;
    
        reso.key = 'Reso';
        %reso.type = 'String';
        %reso.choice = {0,'SemiTone'};
        reso.default = 0;
    option.reso = reso;
    
        resofirst.key = 'First';
        resofirst.type = 'Boolean';
        resofirst.default = 0;
    option.resofirst = resofirst;
    
        resoloose.key = 'Loose';
        resoloose.type = 'Boolean';
        resoloose.default = 0;
    option.resoloose = resoloose;
        
        c.key = 'Pref';
        c.type = 'Integer';
        c.number = 2;
        c.default = [0 0];
    option.c = c;
        
        near.key = 'Nearest';
        near.type = 'Integer';
        near.default = NaN;
    option.near = near;
        
        logsc.type = 'String';
        logsc.choice = {'Lin','Log',0};
        logsc.default = 'Lin';
    option.logsc = logsc;
        
        normal.key = 'Normalize';
        normal.type = 'String';
        normal.choice = {'Local','Global','No',0};
        normal.default = 'Global';
    option.normal = normal;
    
        localfactor.key = 'LocalFactor';
        localfactor.type = 'Integer';
        localfactor.default = 0;
        localfactor.keydefault = .8;
    option.localfactor = localfactor;

        extract.key = 'Extract';
        extract.type = 'Boolean';
        extract.default = 0;
    option.extract = extract;
    
        only.key = 'Only';
        only.type = 'Boolean';
        only.default = 0;
    option.only = only;

        delta.key = 'Track';
        delta.type = 'Integer';
        delta.default = 0;
        delta.keydefault = Inf;
    option.delta = delta;
    
        harmo.key = 'Harmonic';
        harmo.type = 'Integer';
        harmo.default = 0;
        harmo.keydefault = Inf;
    option.harmo = harmo;
    
        maxpeak.key = 'MaxPeak';
        maxpeak.type = 'Integer';
        maxpeak.default = inf;
    option.maxpeak = maxpeak;
    
        mem.key = 'TrackMem';
        mem.type = 'Integer';
        mem.default = 0;
        mem.keydefault = Inf;
    option.mem = mem;
    
        fuse.key = 'Fuse';
        fuse.type = 'Boolean';
        fuse.default = 0;
    option.fuse = fuse;

        shorttrackthresh.key = 'CollapseTracks';
        shorttrackthresh.type = 'Integer';
        shorttrackthresh.default = 0;
        shorttrackthresh.keydefault = 7;
    option.shorttrackthresh = shorttrackthresh;

        scan.key = 'ScanForward'; % specific to mironsets(..., 'Klapuri99')
        scan.default = [];
    option.scan = scan;
    
%         highest.key = 'Highest';
%         highest.type = 'Boolean';
%         highest.default = 0;
%     option.highest = highest;
    
specif.option = option;

varargout = mirfunction(@mirpeaks,orig,varargin,nargout,specif,@init,@main);


function [x type] = init(x,option)
type = mirtype(x);


function p = main(x,option,postoption)
if iscell(x)
    x = x{1};
end
if option.chro
    option.order = 'Abscissa';
elseif option.ranked
    option.order = 'Amplitude';
end
if not(isnan(option.near)) && option.m == 1
    option.m = Inf;
end
x = purgedata(x);
if option.m <= 0
    p = x;
    return
end
d = get(x,'Data');
sr = get(x,'Sampling');
cha = 0; % Indicates when it is possible to represent as a curve along the 
         % Z-axis (channels) instead of the X-axis (initial abscissa).
if isnan(option.first)
    option.first = option.cthr / 2;
end
if isa(x,'mirscalar')
    t = get(x,'FramePos');
    for i = 1:length(d)
        for j = 1:length(d{i})
            d{i}{j} = d{i}{j}';
            if size(t{i},1) == 1
                t{i}{j} = t{i}{j}(1,:,:)';
            else
                t{i}{j} = (t{i}{j}(1,:,:)+t{i}{j}(2,:,:))'/2;
            end
        end
    end
elseif isa(x,'mirsimatrix')
    t = get(x,'FramePos');
    for i = 1:length(t)
        for j = 1:length(t{i})
            t{i}{j} = repmat((t{i}{j}(1,:,:)+t{i}{j}(2,:,:))'/2,...
                                    [1 size(d{i}{j},2) 1]);
        end
    end
elseif isa(x,'mirhisto')
    error('ERROR IN MIRPEAKS: peaks of histogram not considered yet.');
else
    for i = 1:length(d)
        for j = 1:length(d{i})
            if iscell(d{i})
                dij = d{i}{j};
                if ~cha && j == 1 && size(dij,3) > 1 && size(dij,1) == 1
                    cha = 1;
                end
                if cha && j > 1 && size(dij,1) > 1
                    cha = -1;
                end
            end
        end
        for j = 1:length(d{i})
            if iscell(d{i})
                dij = d{i}{j};
                if cha == 1
                    if iscell(dij)
                        for k = 1:size(dij,2)
                            d{i}{j}{k} = reshape(dij{k},size(dij{k},2),size(dij{k},3));
                            d{i}{j}{k} = d{i}{j}{k}';
                        end
                    else
                        d{i}{j} = reshape(dij,size(dij,2),size(dij,3));
                        d{i}{j} = d{i}{j}';
                    end
                end
            end
        end
    end
    if cha == 1
        t = get(x,'Channels');
    else
        t = get(x,'Pos');
    end
end

interpol = get(x,'Interpolable') && not(isempty(option.interpol)) && ...
    ((isnumeric(option.interpol) && option.interpol) || ...
     (ischar(option.interpol) && not(strcmpi(option.interpol,'No')) && ...
      not(strcmpi(option.interpol,'Off'))));
                
pp = cell(1,length(d));
pv = cell(1,length(d));
pm = cell(1,length(d));
ppp = cell(1,length(d));
ppv = cell(1,length(d));
tp = cell(1,length(d));
if interpol
    tpp = cell(1,length(d));
    tpv = cell(1,length(d));
end
tv = cell(1,length(d));

if isnan(option.thr)
    option.thr = 0;
else
    if option.vall
        option.thr = 1-option.thr;
    end
end
%if isnan(option.lthr)
%    option.lthr = 1;
%else
%    if option.vall
%        option.lthr = 1-option.lthr;
%    end
%end
if isnan(option.smthr)
    option.smthr = option.thr - .2;
end

if not(isempty(option.scan))
    pscan = get(option.scan,'PeakPos');
end

for i = 1:length(d) % For each audio file,...
    di = d{i};
    if cha == 1
        ti = t; %sure ?
    else
        ti = t{i};
    end
    if not(iscell(di))
        di = {di};
        if isa(x,'mirchromagram') && not(cha)
            ti = {ti};
        end
    end
    
    if option.vall
        for h = 1:length(di)
            di{h} = -di{h};
        end
    end

    if strcmpi(option.normal,'Global')
        % Normalizing across segments
        madi = zeros(1,length(di));
        midi = zeros(1,length(di));
        for h = 1:length(di)
            if isempty(di{h})
                madi(h) = -Inf;
                midi(h) = Inf;
            else
                madi(h) = max(max(max(max(di{h},[],1),[],2),[],3),[],4);
                midi(h) = min(min(min(min(di{h},[],1),[],2),[],3),[],4);
            end
        end
        mad = max(madi);
        mid = min(midi);
    end
        
    for h = 1:length(di)    % For each segment,...
        dhu = di{h}; % This copy of the data is kept untransformed and used for output.
        if isempty(dhu)
            continue
        end
        dht = dhu; % This copy of the data will be transformed (normalization, etc.)
        
        [nl0 nc np nd0] = size(dhu);
        
        if cha == 1
            if iscell(ti)
                %% problem here!!!
                ti = ti{i}; %%%%%it seems that sometimes we need to use instead ti{i}(:);
            end
            th = repmat(ti,[1,nc,np,nd0]);
        else
            th = ti{h};
            if iscell(th)  % Non-numerical abscissae are transformed into numerical ones. 
                th = repmat((1:size(th,1))',[1,nc,np]);
            else
                if 0 %size(th,2) == 1 && nc>1
                    error('Problematic case. New code below to be used.');
                    th = repmat(th,[1,nc,1]);
                end
                if size(th,3)<np
                    th = repmat(th,[1,1,np]);
                end
            end
        end
        
        if strcmpi(option.normal,'Global')
            % Normalizing across segments
            dht = (dht - repmat(mid,[nl0 nc np nd0]))./... 
                  repmat(mad-mid,[nl0 nc np nd0]);
        end
        
        if option.c    % If a prefered region is specified, the data is amplified accordingly
            dht = dht.*exp(-(th-option.c(1)).^2/2/option.c(2)^2)...
                                    /option.c(2)/sqrt(2*pi)/2;
        end

        % Now the data is rescaled. the minimum is set to 0
        % and the maximum to 1.
        state = warning('query','MATLAB:divideByZero');
        warning('off','MATLAB:divideByZero');
        
        % Why is the following commented out??
%         for l = 1:nd0
%             mht = shiftdim(max(dht(:,:,:,l)),1);
%             [low1,low2] = find(mht<option.thr);
%             dht(:,low1,low2,l) = 0;
%         end
        
        if strcmpi(option.normal,'Local')
            if option.localfactor
                maxl = 0;
                for l = 1:size(dht,2)
                    maxl = max(maxl*option.localfactor,...
                               max(max(max(dht(:,l,:,:),[],1),[],3),[],4));
                    dht(:,l,:,:) = dht(:,l,:,:) / maxl;
                end
            else
                % Normalizing each frame separately:
                dht = (dht-repmat(min(min(dht,[],1),[],4),[nl0 1 1 nd0]))./... 
                    repmat(max(max(dht,[],1),[],4)...
                          -min(min(dht,[],1),[],4),[nl0 1 1 nd0]);
            end
        end
        warning(state.state,'MATLAB:divideByZero');

        szth = size(th);
        szth(1) = 1;
        if option.nobegin
            dht = [Inf(1,nc,np,nd0);dht];   
            % This infinite value at the beginning
            % prevents the selection of the first sample of data
            dhu = [Inf(1,nc,np,nd0);dhu];
            th = [NaN(szth);th];
        else
            dht = [-Inf(1,nc,np,nd0);dht];
            % This infinite negative value at the beginning
            % ensures the selection of the first sample of data
            dhu = [-Inf(1,nc,np,nd0);dhu];
            th = [NaN(szth);th];
        end
        if option.noend
            dht = [dht;Inf(1,nc,np,nd0)];
            % idem for the last sample of data
            dhu = [dhu;Inf(1,nc,np,nd0)];
            th = [th;NaN(szth)];
        else
            dht = [dht;-Inf(1,nc,np,nd0)];
            dhu = [dhu;-Inf(1,nc,np,nd0)];
            th = [th;NaN(szth)];
        end
        nl0 = nl0+2;

        % Rearrange the 4th dimension (if used) into the 1st one.
        nl = nl0*nd0;
        dht4 = zeros(nl,nc,np);
        dhu4 = zeros(nl,nc,np);
        th2 = zeros(size(th));
        for l = 1:nd0
            dhu4((l-1)*nl0+(1:nl0)',:,:) = dhu(:,:,:,l);
            dht4((l-1)*nl0+(1:nl0)',:,:) = dht(:,:,:,l);
            th2((l-1)*nl0+(1:nl0)',:,:) = th(:,:,:);
        end
        dht = dht4;
        dhu = dhu4;
        
        th = th2; % The X-abscissa

        ddh = diff(dht);
        % Let's find the local maxima
        for l = 1:np
            dl = dht(2:end-1,:,l);
            for k = 1:nc
                dk = dl(:,k);
                if isequal(option.normal, 0) || strcmpi(option.normal,'No')
                    mx{1,k,l} = find(and(dk >= option.thr,...     
                                         and(ddh(1:(end-1),k,l) > 0, ...
                                             ddh(2:end,k,l) <= 0)))+1;
                else
                    mx{1,k,l} = find(and(and(dk >= option.cthr, ...
                                             dk >= option.thr),...     
                                             ... dk <= option.lthr)),
                                         and(ddh(1:(end-1),k,l) > 0, ...
                                             ddh(2:end,k,l) <= 0)))+1;
                end
            end
        end
        if option.cthr
            for l = 1:np
                for k = 1:nc
                    finalmxk = [];
                    mxk = mx{1,k,l};
                    if not(isempty(mxk))
                        wait = 0;
                        if (length(mxk)>5000) && mirwaitbar
                            wait = waitbar(0,['Selecting peaks... (0 out of 0)']);
                        end
                        %if option.m < Inf
                        %    [unused,idx] = sort(dh(mxk,k,l),'descend'); % The peaks are sorted in decreasing order
                        %    mxk = mxk(sort(idx(1:option.m)));
                        %end
                        j = 1;  % Scans the peaks from begin to end.
                        mxkj = mxk(j); % The current peak
                        jj = j+1;
                        bufmin = Inf;
                        bufmax = dht(mxkj,k,l);
                        oldbufmin = min(dht(1:mxk(1)-1,k,l));
                        while jj <= length(mxk)
                            if isa(wait,'matlab.ui.Figure') && not(mod(jj,5000))
                                waitbar(jj/length(mxk),wait,['Selecting peaks... (',num2str(length(finalmxk)),' out of ',num2str(jj),')']);
                            end
                            bufmin = min(bufmin, ...
                                min(dht(mxk(jj-1)+1:mxk(jj)-1,k,l)));
                            if bufmax - bufmin < option.cthr
                                % There is no contrastive notch
                                if dht(mxk(jj),k,l) > bufmax && ...
                                        (dht(mxk(jj),k,l) - bufmax > option.first ...
                                        || (bufmax - oldbufmin < option.cthr))
                                    % If the new peak is significantly
                                    % higher than the previous one,
                                    % The peak is transfered to the new
                                    % position
                                    j = jj;
                                    mxkj = mxk(j); % The current peak
                                    bufmax = dht(mxkj,k,l);
                                    oldbufmin = min(oldbufmin,bufmin);
                                    bufmin = Inf;
                                elseif dht(mxk(jj),k,l) - bufmax <= option.first
                                    bufmax = max(bufmax,dht(mxk(jj),k,l));
                                    oldbufmin = min(oldbufmin,bufmin);
                                end
                            else
                                % There is a contrastive notch
                                if bufmax - oldbufmin < option.cthr
                                    % But the previous peak candidate
                                    % is too weak and therefore
                                    % discarded
                                    oldbufmin = min(oldbufmin,bufmin);
                                else
                                    % The previous peak candidate is OK
                                    % and therefore stored
                                    finalmxk(end+1) = mxkj;
                                    oldbufmin = bufmin;
                                end
                                bufmax = dht(mxk(jj),k,l);
                                j = jj;
                                mxkj = mxk(j); % The current peak
                                bufmin = Inf;
                            end
                            jj = jj+1;
                        end
                        if bufmax - oldbufmin >= option.cthr && ...
                                bufmax - min(dht(mxk(j)+1:end,k,l)) >= option.cthr
                            % The last peak candidate is OK and stored
                            finalmxk(end+1) = mxk(j);
                        end
                        if isa(wait,'matlab.ui.Figure')
                            waitbar(1,wait);
                            close(wait);
                            drawnow
                        end
                    end
                    mx{1,k,l} = finalmxk;
                end
            end
        end
        if not(isempty(option.scan)) % 'ScanForward' option, used for 'Klapuri99' in mironsets
            for l = 1:np
                for k = 1:nc   
                    pscankl = pscan{i}{h}{1,k,l}; % scan seed positions
                    mxkl = [];
                    lp = length(pscankl); % number of peaks
                    for jj = 1:lp         % for each peak
                        fmx = find(mx{1,k,l}>pscankl(jj),1);
                            % position of the next max following the
                            % current seed
                        fmx = mx{1,k,l}(fmx);
                        if jj<lp && (isempty(fmx) || fmx>=pscankl(jj+1))
                            [unused fmx] = max(dht(pscankl(jj):...
                                                  pscankl(jj+1)-1,k,l));
                            fmx = fmx+pscankl(jj)-1;
                        elseif jj==lp && isempty(fmx)
                            [unused fmx] = max(dht(pscankl(jj):end,k,l));
                            fmx = fmx+pscankl(jj)-1;
                        end
                        mxkl = [mxkl fmx];
                    end
                    mx{1,k,l} = mxkl;
                end
            end
        end
        if not(isequal(option.reso,0)) % Removing peaks too close to higher peaks
            if ischar(option.reso) && strcmpi(option.reso,'SemiTone')
                compar = @semitone_compar;
            elseif isnumeric(option.reso)
                compar = @dist_compar;
            end
            for l = 1:np
                for k = 1:nc
                    [unused ind] = sort(dht(mx{1,k,l}),'descend');
                    mxlk = mx{1,k,l}(ind);
                    del = [];
                    j = 1;
                    while j < length(mxlk)
                        jj = j+1;
                        while jj <= length(mxlk)
                            if compar(th(mxlk(jj),k,l),th(mxlk(j),k,l),...
                                    option.reso)
                                if option.resoloose
                                    mxlk(jj) = [];
                                    jj = jj-1;
                                elseif option.resofirst && mxlk(j)>mxlk(jj)
                                    del = [del j];
                                else
                                    del = [del jj];
                                end
                            end
                            jj = jj+1;
                        end
                        j = j+1;
                    end
                    if ~option.resoloose
                        mxlk(del) = [];
                    end
                    mx{1,k,l} = mxlk;
                end
            end
        end
        if not(isnan(option.near)) % Finding a peak nearest a given prefered location
            for l = 1:np
                for k = 1:nc
                    mxlk = mx{1,k,l};
                    if strcmp(option.logsc,'Log')
                        [M I] = min(abs(log(th(mxlk,k,l)/option.near)));
                    else
                        [M I] = min(abs(th(mxlk,k,l)-option.near));
                    end
                    mx{1,k,l} = mxlk(I);
                end
            end
        end
        if option.harmo
            tol = .15;
            minamp = .4;
            tp{i}{h} = cell(1,np);
            %if interpol
                tpp{i}{h} = cell(1,np);
                tpv{i}{h} = cell(1,np);
            %end
            for l = 1:np
                mxl = NaN(1,nc);
                txl = NaN(1,nc);
                myl = zeros(1,nc);
                segm = 0;
                current = [];
                for k = 1:nc
                    mxk = mx{1,k,l};        
                    txk = th(mxk,k);
                    myk = dht(mxk,k);
                    
                    ff = find(txk < option.maxpeak);
                    txkm = txk(ff);
                    mykm = myk(ff);
                    
                    if isempty(txkm)
                        segm = 0;
                        current = [];
                        continue
                    end
                    
                    [maxk idx] = max(mykm);
                    
                    if maxk < minamp
                        segm = 0;
                        current = [];
                        continue
                    end
                    
                    if idx > 1
                        if k > 1 && ~isempty(find(txl(:,k-1)))
                            [unused r] = min(abs(txk(idx)-txl(:,k-1)));
                            if r > 1
                                [unused ik] = min(abs(txk(idx)/r - th(:,k)));
                                if dht(ik,k) / maxk > .8
                                    mxl(1,k) = ik;
                                    txl(1,k) = th(ik,k);
                                    myl(1,k) = dht(ik,k);
                                    idx = 0;
                                    current = th(ik,k);
                                end
                            end
                        end
                        if idx
                            idxr = [];
                            r = [];
                            if k > 1 && ~isempty(find(txl(:,k-1)))
                                [unused r] = min(abs(txk-txl(1,k-1)));
                            end
                            for n = 1:idx-1
                                if myk(n)/myk(idx) < .5
                                    continue
                                end
                                if n == r
                                    idx = n;
                                    idr = [];
                                    break
                                end
                                harmo = mod(txk(idx)/txk(n),1);
                                if round(txk(idx)/txk(n)) > 1 && ...
                                        (harmo < tol || harmo > 1-tol)
                                    idxr(end+1) = n;
                                end
                            end
                            if ~isempty(idxr)
                                [unused best] = max(myl(idxr));
                                idx = idxr(best);
                            end
                        end
                    end
                    
%                     if ~isempty(constant)
%                         [unused r] = min(abs(txk - constant.t));
%                         if myk(r) / maxk < 1
%                             constant = [];
%                         else
%                             mat = max(txk(r),constant.t);
%                             mit = min(txk(r),constant.t);
%                             if mat/mit > 1.2
%                                 constant = [];
%                             else
%                                 constant.t = txk(r);
%                             end
%                         end
%                     end
%                     if isempty(constant)
%                         constant.k = k;
%                         constant.t = txk(idx);
%                     elseif ~isnan(txl(1,k-1))
%                         [unused r] = min(abs(txk-txl(1,k-1)));
%                         if r == idx 
%                             if r > 1 && txk(r-1)/txk(r) > .7 && mxk(r-1) > mxk(r) / 2
% %                                 1
%                             elseif r < length(txk) && txk(r)/txk(r+1) > .7 && mxk(r+1) > mxk(r) / 2
% %                                 1
%                             end
%                         end
%                     end

                    %idx = find(myk>.7);
                    %if isempty(idx)
                    %    continue
                    %end
                    
                    %if k == 1
                    %    idx = idx(1);
                    %else
                    %    [unused c] = min(abs(txk(idx)-txl(1,k-1)));
                    %    idx = idx(c);
                    %end
                    
                    if idx
                        if ~segm
                            segm = k;
                        elseif 1
%                             dist = abs(txk - txl(1,k-1));
%                             [unused idx2] = min(dist);
%                             if idx2 ~= idx 
%                                 mat = max(txk(idx2),txl(1,k-1));
%                                 mit = min(txk(idx2),txl(1,k-1));
%                                 if mat/mit<1.025
%                                     idx = idx2;
%                                 end
%                             end
                            mat = max(txl(1,k-1),txk(idx));
                            mit = min(txl(1,k-1),txk(idx));
                            if mat/mit > 1.2
%                                 if txk(idx) < txl(1,k-1) %k - segm < 2
%                                     mxl(:,segm:k-1) = NaN;
%                                     txl(:,segm:k-1) = NaN;
%                                     myl(:,segm:k-1) = 0;
%                                 else
%                                     
%                                 end
                                segm = k;
                            end
                        end

                        %ser = cell(1,length(mxk));
                        %for n1 = 1:length(mxk)
                        %    ser{n1} = 1;
                        %    for n2 = n1+1:length(mxk)
                        %        harmo = mod(txk(n2)/txk(n1),1);
                        %        rk = round(txk(n2)/txk(n1));
                        %        if rk > 1 && ~ismember(rk,ser{n1}) && ...
                        %                harmo <.2 || harmo > .8
                        %            ser{n1}(end+1) = rk;
                        %        end
                        %    end
                        %end
                        %ser;

                        mxl(1,k) = mxk(idx)-1;
                        txl(1,k) = txk(idx);
                        myl(1,k) = myk(idx);
                        current = txk(idx);
                    end

                    for n = 1:length(mxk)
                        if mxk(n) <= mxl(1,k)
                            continue
                        end
                        harmo = mod(txk(n)/txl(1,k),1);
                        rk = round(txk(n)/txl(1,k));
                        if ((size(mxl,1) < rk || myk(n) > myl(rk,k)) && ...
                                harmo < tol || harmo > 1-tol) && ...
                                rk <= option.harmo
                            if rk > size(mxl,1)
                                mxl(size(mxl)+1:rk,:) = NaN;
                                txl(size(mxl):rk,:) = NaN;
                                myl(size(mxl):rk,:) = 0;
                            end
                            mxl(rk,k) = mxk(n);
                            txl(rk,k) = txk(n);
                            myl(rk,k) = myk(n);
                        end
                    end
                    
%                     if size(myl,1) > 2 && isnan(myl(2,k))
%                         mxl(:,k) = NaN;
%                         txl(:,k) = NaN;
%                         myl(:,k) = NaN;
%                     end
                end
                tp{i}{h}{l} = mxl;
                tpp{i}{h}{l} = txl;
                tv{i}{h}{l} = myl;
                tpv{i}{h}{l} = myl;
            end
        elseif option.delta % Peak tracking
            tp{i}{h} = cell(1,np);
            if interpol
                tpp{i}{h} = cell(1,np);
                tpv{i}{h} = cell(1,np);
            end
            for l = 1:np
                % mxl will be the resulting track position matrix
                % and myl the related track amplitude
                % In the first frame, tracks can be identified to peaks.
                mxl = mx{1,1,l}(:)-1;        
                myl = dht(mx{1,1,l}(:),k,l); 
                                
                % To each peak is associated the related track ID
                tr2 = 1:length(mx{1,1,l});
                
                grvy = []; % The graveyard.
                
                wait = 0;
                if nc-1>500
                    wait = waitbar(0,['Tracking peaks...']);
                end

                for k = 1:nc-1
                    % For each successive frame...
                    
                    if not(isempty(grvy))
                        old = find(grvy(:,2) == k-option.mem-1);
                        grvy(old,:) = [];
                    end
                    
                    if wait && not(mod(k,100))
                        waitbar(k/(nc-1),wait);
                    end

                    mxk1 = mx{1,k,l};   % w^k
                    mxk2 = mx{1,k+1,l}; % w^{k+1}
                    thk1 = th(mxk1,k,l);
                    thk2 = th(mxk2,k,l);
                    matched = zeros(size(thk2));
                    myk2 = dht(mx{1,k+1,l},k,l); % amplitude
                    tr1 = tr2;
                    tr2 = NaN(1,length(mxk2));
                    
                    mxl(:,k+1) = mxl(:,k);
                                        
                    if isempty(thk1) || isempty(thk2)
                        %% IS THIS TEST NECESSARY??
                        
                        myl(:,k+1) = 0;
                    else
                        for n = 1:length(mxk1)
                            % Let's check each track.
                            tr = tr1(n); % Current track.

                            if not(isnan(tr))
                                % track currently active

                                % Step 1 in Mc Aulay & Quatieri
                                [int m] = min(abs(thk2-thk1(n)));
                                % Finding w^{k+1} closest to current w^k
                                
                                if isinf(int) || int > option.delta
                                    % all w^{k+1} outside matching interval:
                                        % partial becomes inactive
                                    mxl(tr,k+1) = mxl(tr,k);
                                    myl(tr,k+1) = 0;
                                    grvy = [grvy; tr k]; % added to the graveyard
                                else
                                    [best mm] = min(abs(thk2(m)-th(mxk1,k,l)));
                                    % the mmth peak in frame k is the closest to w^{k+1}

                                    % Let's first test whether candidate
                                    % match w^{k+1} is particularly closed to an inactive track. (Lartillot)
                                    if isempty(grvy)
                                        testprev = 0;
                                    else
                                        [best2 mm2] = min(abs(thk2(m)-th(mxl(grvy(:,1),k),k,l)));
                                        if best2 < best
                                            oldk = grvy(mm2,2);
                                            if mxl(tr,oldk)
                                                oldt1 = th(mxl(grvy(mm2,1),oldk),oldk,l);
                                                oldt2 = th(mxl(tr,oldk),oldk,l);
                                                dif1 = abs(oldt1-thk2(m));
                                                dif2 = abs(oldt2-thk2(m));
                                                if dif1 < dif2
                                                    testprev = 1;
                                                end
                                            else
                                                testprev = 1;
                                            end
                                        else
                                            testprev = 0;
                                        end
                                    end
                                    if testprev
                                        % Yes, candidate match w^{k+1} is particularly closed to an inactive track. (Lartillot)
                                        otr = grvy(mm2,1);
                                        mxl(otr,k+1) = mxk2(m)-1;
                                        myl(otr,k+1) = myk2(m);
                                        tr2(m) = otr;
                                        thk2(m) = Inf;  % selected w^{k+1} is eliminated from further consideration
                                        matched(m) = 1;
                                        grvy(mm2,:) = [];
                                        
                                    else
                                        % Step 2 in Mc Aulay & Quatieri
                                        if option.fuse || mm == n
                                            % candidate match w^{k+1} is not closer to any remaining w^k:
                                            % definite match
                                            mxl(tr,k+1) = mxk2(m)-1;
                                            myl(tr,k+1) = myk2(m);
                                            tr2(m) = tr;
                                            matched(m) = 1;
                                            if ~option.fuse
                                                thk1(n) = -Inf; % selected w^k is eliminated from further consideration
                                                thk2(m) = Inf;  % selected w^{k+1} is eliminated as well
                                            end
                                            if not(isempty(grvy))
                                                zz = find ((mxl(grvy(:,1),k) >= mxl(tr,k) & ...
                                                            mxl(grvy(:,1),k) <= mxl(tr,k+1)) | ...
                                                           (mxl(grvy(:,1),k) <= mxl(tr,k) & ...
                                                            mxl(grvy(:,1),k) >= mxl(tr,k+1)));
                                                grvy(zz,:) = [];
                                            end
                                        end
                                        
                                        if ~option.fuse && mm ~= n
                                            % candidate match w^{k+1} is closer to another w^k
                                            
                                            % let's look at adjacent lower w^{k+1}...
                                            [int mmm] = min(abs(thk2(1:m)-thk1(n)));
                                            if int > best || ... % New condition added (Lartillot 16.4.2010)
                                                    isinf(int) || ... % Conditions proposed in Mc Aulay & Quatieri (all w^{k+1} below matching interval)
                                                    int > option.delta
                                                % no other suitable candidate match w^{k+1} found
                                                % partial becomes inactive
                                                mxl(tr,k+1) = mxl(tr,k);
                                                myl(tr,k+1) = 0;
                                                grvy = [grvy; tr k]; % added to the graveyard
                                            else
                                                
                                                % definite match
                                                mxl(tr,k+1) = mxk2(mmm)-1;
                                                myl(tr,k+1) = myk2(mmm);
                                                tr2(mmm) = tr;
                                                thk1(n) = -Inf;     % selected w^k is eliminated from further consideration
                                                thk2(mmm) = Inf;    % selected w^{k+1} is eliminated as well
                                                matched(mmm) = 1;
                                                if not(isempty(grvy))
                                                    zz = find ((mxl(grvy(:,1),k) >= mxl(tr,k) & ...
                                                                mxl(grvy(:,1),k) <= mxl(tr,k+1)) | ...
                                                               (mxl(grvy(:,1),k) <= mxl(tr,k) & ...
                                                                mxl(grvy(:,1),k) >= mxl(tr,k+1)));
                                                    grvy(zz,:) = [];
                                                end
                                            end
                                        end
                                    end
                                end
                            end
                        end
                    end
                    
                    
                    % Step 3 in Mc Aulay & Quatieri
                    for m = 1:length(mxk2)
                        if ~matched(m)
                            % unmatched w^{k+1}
                            if isempty(grvy)
                                int = [];
                            else
                                % Let's try to reuse an inactive track from the
                                % graveyard (Lartillot).
                                [int z] = min(abs(th(mxl(grvy(:,1),k+1)+1,k,l)-thk2(m)));
                            end
                            if isempty(int) || int > option.delta ...
                                    || int > min(abs(th(mxl(:,k+1)+1,k,l)-thk2(m)))
                                % No suitable inactive track.
                                % birth of a new partial (Mc Aulay &
                                % Quatieri)
                                mxl = [mxl;zeros(1,k+1)];
                                tr = size(mxl,1);
                                mxl(tr,k) = mxk2(m)-1;
                            else
                                % Suitable inactive track found, turned active. (Lartillot)
                                tr = grvy(z,1);
                                grvy(z,:) = [];
                            end
                            mxl(tr,k+1) = mxk2(m)-1;
                            myl(tr,k+1) = myk2(m);
                            tr2(m) = tr;
                        end
                    end
                end
                
                if wait
                    waitbar(1,wait);
                    close(wait);
                    drawnow
                end

                if size(mxl,1) > option.m
                    tot = sum(myl,2);
                    [tot ix] = sort(tot,'descend');
                    mxl(ix(option.m+1:end),:) = [];
                    myl(ix(option.m+1:end),:) = [];
                end
                
                mxl(:,not(max(myl))) = 0;
                
                if option.shorttrackthresh
                    [myl bestrack] = max(myl,[],1);
                    mxl = mxl(bestrack + (0:size(mxl,2)-1)*size(mxl,1));
                    changes = find(not(bestrack(1:end-1) == bestrack(2:end)))+1;
                    if not(isempty(changes))
                        lengths = diff([1 changes nc+1]);
                        shorts = find(lengths < option.shorttrackthresh);
                        for k = 1:length(shorts)
                            if shorts(k) == 1
                                k1 = 1;
                            else
                                k1 = changes(shorts(k)-1);
                            end
                            k2 = k1 + lengths(shorts(k)) -1;
                            myl(1,k1:k2) = 0;
                            mxl(1,k1:k2) = 0;
                        end
                    end
                end
                
                tp{i}{h}{l} = mxl;
                tv{i}{h}{l} = myl;

                if interpol  
                    tpv{i}{h}{l} = zeros(size(mxl));
                    tpp{i}{h}{l} = zeros(size(mxl));
                    for k = 1:size(mxl,2)
                        for j = 1:size(mxl,1)
                            if myl(j,k)
                                mj = mxl(j,k);
                                if mj>2 && mj<size(dhu,1)-1
                                    % More precise peak position
                                    y0 = dhu(mj,k,l);
                                    ym = dhu(mj-1,k,l);
                                    yp = dhu(mj+1,k,l);
                                    p = (yp-ym)/(2*(2*y0-yp-ym));
                                    tpv{i}{h}{l}(j,k) = y0 - 0.25*(ym-yp)*p;
                                    if p >= 0
                                        tpp{i}{h}{l}(j,k) = (1-p)*th(mj,k,l)+p*th(mj+1,k,l);
                                    elseif p < 0
                                        tpp{i}{h}{l}(j,k) = (1+p)*th(mj,k,l)-p*th(mj-1,k,l);
                                    end
                                elseif mj
                                    tpv{i}{h}{l}(j,k) = dhu(mj,k,l);
                                    tpp{i}{h}{l}(j,k) = th(mj,k,l);
                                end
                            else
                                tpv{i}{h}{l}(j,k) = 0;
                                tpp{i}{h}{l}(j,k) = NaN;
                            end
                        end
                    end
                end
            end
        end
        if isa(x,'mirsimatrix') && option.graph
            % Finding the best branch inside a graph constructed out of a
            % similarity matrix
            g{i}{h} = cell(1,nc,np);
                % Branch info related to each peak
            br{i}{h} = {};
                % Info related to each branch
            scog{i}{h} = cell(1,nc,np);
                % Score related to each peak
            scob{i}{h} = [];
                % Score related to each branch
            for l = 1:np
                wait =0 % waitbar(0,['Creating peaks graph...']);
                for k = 1:nc
                    g{i}{h}{1,k,l} = cell(size(mx{1,k,l}));
                    scog{i}{h}{1,k,l} = zeros(size(mx{1,k,l}));
                    if wait && not(mod(k,50))
                        waitbar(k/(nc-1),wait);
                    end
                    mxk = mx{1,k,l}; % Peaks in current frame
                    for j = k-1:-1:max(1,k-10) % Recent frames
                        mxj = mx{1,j,l};        % Peaks in one recent frame
                        for kk = 1:length(mxk)
                            mxkk = mxk(kk);     % For each of current peaks
                            if mxkk < 10
                                continue
                            end
                            for jj = 1:length(mxj)
                                mxjj = mxj(jj); % For each of recent peaks
                                sco = k-j - abs(mxkk-mxjj);
                                    % Crossprogression from recent to
                                    % current peak
                                if sco >= 0 
                                        % Negative crossprogression excluded
                                    dist = 0;
                                    % The distance between recent and
                                    % current peak is the sum of all the
                                    % simatrix values when joining the two
                                    % peaks with a straight line.
                                    for m = j:k
                                        % Each point in that straight line.
                                        mxm = mxjj + (mxkk-mxjj)*(m-j)/(k-j);
                                        if mxm == floor(mxm)
                                            dist = dist + 1-dht(mxm,m,l);
                                        else
                                            dhm0 = dht(floor(mxm),m,l);
                                            dhm1 = dht(ceil(mxm),m,l);
                                            dist = dist + 1-...
                                                (dhm0 + ...
                                                 (dhm1-dhm0)*(mxm-floor(mxm)));
                                        end
                                        if dist > option.graph
                                            break
                                        end
                                    end
                                    if dist < option.graph
                                        % If the distance between recent
                                        % and current peak is not too high,
                                        % a new edge is formed between the
                                        % peaks, and added to the graph.
                                        gj = g{i}{h}{1,j,l}{jj};
                                            % Branch information associated
                                            % with recent peak
                                        gk = g{i}{h}{1,k,l}{kk};
                                            % Branch information associated
                                            % with current peak
                                        if isempty(gk) || ...
                                                sco > scog{i}{h}{1,k,l}(kk)
                                            % Current peak branch to be updated
                                            if isempty(gj)
                                                % New branch starting
                                                % from scratch
                                                newsco = sco;
                                                scob{i}{h}(end+1) = newsco;
                                                bid = length(scob{i}{h});
                                                g{i}{h}{1,j,l}{jj} = ...
                                                    [k kk bid newsco];
                                                br{i}{h}{bid} = [j jj;k kk];
                                            else
                                                newsco = scog{i}{h}{1,j,l}(jj)+sco;
                                                if length(gj) == 1
                                                    % Recent peak not
                                                    % associated with other
                                                    % branch
                                                    % -> Branch extension
                                                    bid = gj;
                                                    g{i}{h}{1,j,l}{jj} = ...
                                                        [k kk bid newsco];
                                                    br{i}{h}{bid}(end+1,:) = [k kk];
                                                else
                                                    % Recent peak already
                                                    % associated with other
                                                    % branch
                                                    % -> Branch fusion
                                                    bid = length(scob{i}{h})+1;
                                                    g{i}{h}{1,j,l}{jj} = ...
                                                        [k kk bid newsco; gj];
                                                    other = br{i}{h}{gj(1,3)};
                                                        % Other branch
                                                        % info
                                                        % Let's copy its
                                                        % prefix to the new
                                                        % branch:
                                                    other(other(:,1)>j,:) = [];
                                                    br{i}{h}{bid} = [other;k kk];
                                                end
                                                scob{i}{h}(bid) = newsco;
                                            end
                                            g{i}{h}{1,k,l}{kk} = bid;
                                                % New peak associated with
                                                % branch
                                            scog{i}{h}{1,k,l}(kk) = newsco;
                                        end
                                    end
                                end
                            end
                        end
                    end
                end
                [scob{i}{h} IX] = sort(scob{i}{h},'descend');
                if length(IX) > option.m
                    scob{i}{h} = scob{i}{h}(1:option.m);
                    IX = IX(1:option.m);
                end
                    % Branch are ordered from best score to lowest
                br{i}{h} = br{i}{h}(IX);
                if wait
                    waitbar(1,wait);
                    close(wait);
                    drawnow
                end
            end
        end
        if ~option.graph
            for l = 1:np % Orders the peaks and select the best ones
                for k = 1:nc
                    mxk = mx{1,k,l};
                    if length(mxk) > option.m 
                        [unused,idx] = sort(dht(mxk,k,l),'descend');
                        idx = idx(1:option.m);
                    elseif strcmpi(option.order,'Amplitude')
                        [unused,idx] = sort(dht(mxk,k,l),'descend');
                    else
                        idx = 1:length(dht(mxk,k,l));
                    end
                    if strcmpi(option.order,'Abscissa')
                        mx{1,k,l} = sort(mxk(idx));
                    elseif strcmpi(option.order,'Amplitude')
                        mx{1,k,l} = mxk(idx);
                    end
                    
                    %% Alternate code, allowing using order to get first peak:
%                     if strcmpi(option.order,'Amplitude')
%                         [unused,idx] = sort(dht(mxk,k,l),'descend');
%                     else
%                         idx = 1:length(dht(mxk,k,l));
%                     end
%                     if length(mxk) > option.m 
%                         idx = idx(1:option.m);
%                     end
%                     mx{1,k,l} = mxk(idx);
                end
            end
        end
        if option.extract % Extracting the positive part of the curve containing the peaks
            if isa(x,'mirtemporal')
                filn = floor(sr{i}/25);
            else
                filn = 10;
            end
            if filn>1 && size(dhu,1)>5
                filn = min(filn,floor(size(dhu,1)/3));
                fild = filtfilt(ones(1,filn)/2,1,dhu(2:end-1,:,:))/filn/2;
            else
                fild = dhu(2:end-1,:,:);
            end
            fild = [zeros(1,size(fild,2),size(fild,3));diff(fild)];
            for l = 1:np
                for k = 1:nc
                    idx = 1:size(dht,1);
                    mxlk = sort(mx{1,k,l}-1);
                    for j = 1:length(mxlk)
                        
                        if fild(mxlk(j),k,l) < 0
                            bef0 = find(fild(1:mxlk(j)-1,k,l)>=0);
                            if isempty(bef0)
                                bef0 = [];
                            end
                        else
                            bef0 = mxlk(j)-1;
                        end
                        if isempty(bef0)
                            bef = 0;
                        else
                            bef = find(fild(1:bef0(end),k,l)<=0);
                            if isempty(bef)
                                bef = 0;
                            end
                        end
                        if j>1 && bef(end)<aft(1)+2
                            idx(mxlk(j-1):mxlk(j)) = 0;
                            [unused btw] = min(dhu(mxlk(j-1)+1:mxlk(j)+1,k,l));
                            btw = btw+mxlk(j-1);
                            idx(btw-2:btw+2) = btw-2:btw+2;
                            bef = btw+2;
                        end
                        
                        if fild(mxlk(j),k,l) > 0
                            aft0 = find(fild(mxlk(j)+1:end,k,l)<=0)+mxlk(j);
                            if isempty(aft0)
                                aft0 = [];
                            end
                        else
                            aft0 = mxlk(j)+1;
                        end
                        if isempty(aft0)
                            aft = size(d{i}{h},1)+1;
                        else
                            aft = find(fild(aft0(1):end,k,l)>=0)+mxlk(j);
                            if isempty(aft)
                                aft = size(d{i}{h},1)+1;
                            end
                        end
                        
                        idx(bef(end)+3:aft(1)-3) = 0;
                    end
                    idx = idx(find(idx));
                    dhu(idx,k,l) = NaN;
                end
            end
        end
        if option.vall
            dhu = -dhu;
        end
%         if option.highest
%             for l = 1:np
%                 prev = [];
%                 buf = [];
%                 low = 0;
%                 for k = 1:nc
%                     mk = max(mx{1,k,l});
%                     if isempty(mk)
%                         if low && k - start < 30
%                             for g = start:k-1
%                                 mx{1,g,l} = [];
%                             end
%                         end
%                     else
%                         if isempty(prev)
%                             start = k;
%                         else
%                             if abs(log2(th(mk,1)/th(prev,1))) > .2
%                                 if mk > prev
%                                     if k - start < 30
%                                         for g = start:k-1
%                                             mx{1,g,l} = [];
%                                         end
%                                     end
%                                 else
%                                     low = 1;
%                                 end
%                                 start = k;
%                             end
%                         end
%                         mx{1,k,l} = mk;
%                     end
%                     if ~isempty(mk)
%                         buf(end+1) = mk;
%                     end
%                     prev = mx{1,k,l};
%                 end
%             end
%         end
        mmx = cell(1,nc,np);
        mmy = cell(1,nc,np);
        mmv = cell(1,nc,np);
        for l = 1:np
            for k = 1:nc
                mmx{1,k,l} = mod(mx{1,k,l}(:,:,1),nl0)-1;
                mmy{1,k,l} = ceil(mx{1,k,l}/nl0);
                mmv{1,k,l} = dhu(mx{1,k,l}(:,:,1),k,l);
            end
        end
        pp{i}{h} = mmx;
        pm{i}{h} = mmy;
        pv{i}{h} = mmv;
        if not(interpol)
            ppp{i}{h} = {};
            ppv{i}{h} = {};
        else % Interpolate to find the more exact peak positions
            pih = cell(1,nc,np);
            vih = cell(1,nc,np);
            for l = 1:np
                for k = 1:nc
                    mxlk = mx{1,k,l};
                    vih{1,k,l} = zeros(length(mxlk),1);
                    pih{1,k,l} = zeros(length(mxlk),1);
                    for j = 1:length(mxlk)
                        mj = mxlk(j); % Current values
                        if strcmpi(option.interpol,'quadratic')
                            if mj>2 && mj<size(dhu,1)-1
                                % More precise peak position
                                y0 = dhu(mj,k,l);
                                ym = dhu(mj-1,k,l);
                                yp = dhu(mj+1,k,l);
                                p = (yp-ym)/(2*(2*y0-yp-ym));
                                vih{1,k,l}(j) = y0 - 0.25*(ym-yp)*p;
                                if p >= 0
                                    pih{1,k,l}(j) = (1-p)*th(mj,k,l)+p*th(mj+1,k,l);
                                elseif p < 0
                                    pih{1,k,l}(j) = (1+p)*th(mj,k,l)-p*th(mj-1,k,l);
                                end
                            else
                                vih{1,k,l}(j) = dhu(mj,k,l);
                                pih{1,k,l}(j) = th(mj,k,l);
                            end
                        end
                    end
                end
            end
            ppp{i}{h} = pih;
            ppv{i}{h} = vih;
        end
        if not(iscell(d{i})) % for chromagram
            d{i} = dhu(2:end-1,:,:,:);
        else
            if cha == 1
                d{i}{h} = zeros(1,size(dhu,2),size(dhu,1)-2);
                for k = 1:size(dhu,2)
                     d{i}{h}(1,k,:) = dhu(2:end-1,k);
                end
            else
                d{i}{h} = dhu(2:end-1,:,:,:);
            end
        end
        if option.only
            dih = zeros(size(d{i}{h}));
            for l = 1:np
                for k = 1:nc
                    dih(pp{i}{h}{1,k,l},k,l) = ...
                        d{i}{h}(pp{i}{h}{1,k,l},k,l);
                end
            end
            d{i}{h} = dih;
        end
    end
end
p = set(x,'PeakPos',pp,'PeakVal',pv,'PeakMode',pm);
if interpol
   p = set(p,'PeakPrecisePos',ppp,'PeakPreciseVal',ppv);
end
if option.extract
    p = set(p,'Data',d);
end
empty = cell(1,length(d));
if option.only
    p = set(p,'Data',d,'PeakPos',empty,'PeakVal',empty,'PeakMode',empty);
end
if option.harmo || option.delta
    p = set(p,'TrackPos',tp,'TrackVal',tv);
    if interpol
       p = set(p,'TrackPrecisePos',tpp,'TrackPreciseVal',tpv);
    end
end
if isa(x,'mirsimatrix') && option.graph
    p = set(p,'Graph',g,'Branch',br);
end


function y = semitone_compar(p1,p2,thres)
y = max(p1,p2)/min(p1,p2) < 2^(1/12);


function y = dist_compar(p1,p2,thres)
y = abs(p1-p2) < thres;
