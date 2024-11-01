function res = displot(x,y,xlab,ylab,t,fp,pp,tp,tv,ch,multidata,pm,op,ap,rp,of,cl,ax)
% graphical display of any data (except mirscalar data) computed by MIRToolbox

if isempty(y)
    res = 0;
    return
end
manychannels = 0;
y1 = y{1};
if length(y) == 1 
    y = y{1};
    if length(x) == 1
        x = x{1};
    end
end

if isempty(y)
    res = 0;
    return
end

if isstruct(cl)
    y = cl.centr(:,cl.index);
end

if isempty(ax)
    figure
end

fp2 = uncell(fp);
c = size(y,2);
lx = size(y,1);
l = size(y1,3);     % Number of channels
il = (1-0.15)/l;

if iscell(y)
    if size(y{1},3) > 1 && size(y{1},1) == 1  
        for j = 1:c
            y{j} = reshape(y{j},size(y{j},2),size(y{j},3));
            y{j} = y{j}';
        end
        lx = l;
        l = 1;
    end
else
    if l > 1 && lx == 1
        y = reshape(y,size(y,2),size(y,3));
        y = y';
        x = repmat(ch,[1 size(y,2) size(y,3)]);
        lx = l;
        l = 1;
    end
end
if not(iscell(y)) && l > 20
    manychannels = 1;
    if lx == 1
        y = reshape(y,[c l])';
        lx = l;
        l = 1;
    elseif c == 1
        y = reshape(y,[lx l])';
        fp = reshape(x,[1 lx size(x,3)]);
        fp = fp(:,:,1);
        fp2 = fp;
        c = l;
        l = 1;
        if isempty(ch)
            x = 1:size(y,1);
        else
            x = ch';
        end
    else
        mirerror('display','This data cannot be displayed.');
    end
end
curve = (not(iscell(y)) && not(isequal(fp2,0)) && size(fp2,2)==1) || ...
        c == 1 || (strcmp(xlab,'time (s)') && not(manychannels));
if curve
    for i = 1:l
        if l>1
            subplot(l,1,l-i+1,'align');
        end
        if not(iscell(y))
            y = {y};
            x = {x};
        end
        col = cell(length(y));
        for h = 1:length(y)
            col{h} = zeros(size(y{h},2),3,size(y{h},4));
            for j = 1:size(y{h},2)
                if ischar(x{h})
                    xj = x{h};
                else
                    xj = x{h}(:,j);
                end
                yj = y{h}(:,j,i,:);
                lk = size(yj,4);
                for k = 1:lk
                    if c > 1
                        col{h}(j,:,k) = rand(1,3);
                    else
                        col{h}(j,:,k) = num2col(k);
                    end
                    yk = yj(:,:,:,k);
                    if not(isempty(yk))
                        if iscell(xj)
                            lj = length(xj);
                            abs = 0:lj-1;
                        elseif ischar(xj)
                            abs = 1:length(yk);
                        else
                            abs = xj;
                        end
                        if length(abs) < 20 && lk == 1 && c == 1
                            bar(abs,yk);
                        else
                            plot(abs,yk,'Color',col{h}(j,:,k));
                        end
                        if iscell(xj)
                            tick = 0:ceil(lj/14):lj-1;
                            set(gca,'xtick',tick);
                            set(gca,'xticklabel',xj(tick+1));
                        elseif ischar(xj)
                            set(gca,'xticklabel',x);
                        end
                        hold on
                        axis tight
%                         axis 'auto y'
                    end
                end
            end
            if length(y) > 1
                if isempty(cl)
                    colr = h;

                elseif length(cl) == 1
                    colr = cl;
                else
                    colr = cl(h);
                end
                if not(isempty(x{h}))
                    yhi = y{h}(:,:,i);
                    yhi(isnan(yhi)) = [];
                    yhi(isinf(yhi)) = [];
                    if ~isempty(yhi)
                        rectangle('Position',[x{h}(1),...
                                              min(min(yhi)),...
                                              x{h}(end)-x{h}(1),...
                                              max(max(yhi))-...
                                              min(min(yhi))]+1e-16,...
                                  'EdgeColor',num2col(colr),...
                                  'Curvature',.1,'LineWidth',1)
                    end
                end
            end
        end
        if not(isempty(multidata))
            legend(multidata{:},'Location','Best')
        end
        for h = 1:length(y)
            for j = 1:size(y{h},2)
                xj = x{h}(:,j);
                yj = y{h}(:,j,i,:);
                lk = size(yj,4);
                for k = 1:lk
                    yk = yj(:,:,:,k);
                    if not(isempty(yk))
                        if size(y{h},2) > 1
                            rectangle('Position',[xj(1),min(yk),...
                                xj(end)-xj(1),max(yk)-min(yk)]+1e-16,...
                                'EdgeColor',col{h}(j,:,k))
                        end
                        if length(pp) >= h && not(isempty(pp{h}))
                            [ppj order] = sort(pp{h}{1,j,i});
                            if not(isempty(pm)) && not(isempty(pm{1}))
                                pmj = pm{h}{1,j,i}(order);
                                [R C] = find(pmj==k);
                                if iscell(xj)
                                    plot(ppj(R,C)-1,yk(ppj(R,C)),'or') 
                                else
                                    plot(xj(ppj(R,C)),yk(ppj(R,C)),'or') 
                                end
                            else
                                if lk > 1
                                    [R C] = find(ppj(:,:,2)==k);
                                    plot(xj(ppj(R,C)),yk(ppj(R,C)),'or')
                                else
                                    plot(xj(ppj),yk(ppj),'or')
                                end
                            end
                        end
                        if not(isempty(op)) && not(isempty(op{h}))
                            opj = op{h}{1,j,i};
                            if ~isempty(opj)
                                apj = ap{h}{1,j,i};
                                plot(xj(opj),yk(opj),'dm')
                                plot(xj(apj),yk(apj),'dm')
                                for g = 1:length(opj)
                                    line([xj(opj(g)),xj(apj(g))],...
                                        [yk(opj(g)),yk(apj(g))],...
                                        'Color','m')
                                end
                            end
                        end
                        if not(isempty(of)) && not(isempty(of{h}))
                            ofj = of{h}{1,j,i};
                            if ~isempty(ofj)
                                rpj = rp{h}{1,j,i};
                                plot(xj(ofj),yk(ofj),'dm')
                                plot(xj(rpj),yk(rpj),'dm')
                                for g = 1:length(ofj)
                                    line([xj(rpj(g)),xj(ofj(g))],...
                                        [yk(rpj(g)),yk(ofj(g))],...
                                        'Color','m')
                                end
                            end
                        end
                    end
                end
            end
        end
        if i == l
            title(t)
        end
        if i == 1
            xlabel(xlab)
        end
        if l > 1
            if isempty(ch)
                num = i;
            else
                num = ch(i);
            end
            pos = get(gca,'Position');
            axes('Position',[pos(1)-.05 pos(2)+pos(4)/2 .01 .01],'Visible','off');
            text(0,0,num2str(num),'FontSize',12,'Color','r')
        end
        axis tight
    end
else
    % 2-dimensional image
    displayseg = 0;
    if iscell(x) && ischar(x{1})
        if size(y,4) > 1 || size(y,1) > size(x,1)
            ticky = 0.5:23.5;
            tickylab = {'CM','C#M','DM','D#M','EM','FM','F#M',...
            'GM','G#M','AM','A#M','BM','Cm','C#m','Dm','D#m','Em',...
            'Fm','F#m','Gm','G#m','Am','A#m','Bm'};
        else
            ticky = (1:size(x,1))';
            tickylab = x;
            displayseg = 1;
        end
        x = (1:size(y,1))';
    elseif iscell(x) && iscell(x{1}) && ischar(x{1}{1})
        if size(y{1},4) > 1
            ticky = 0.5:23.5;
            tickylab = {'CM','C#M','DM','D#M','EM','FM','F#M',...
            'GM','G#M','AM','A#M','BM','Cm','C#m','Dm','D#m','Em',...
            'Fm','F#m','Gm','G#m','Am','A#m','Bm'};
        else
            ticky = (1:size(x{1},1))';
            tickylab = x{1};
            displayseg = 1;
        end
        for i = 1:length(x)
            x{i} = (1:size(x{i},1))';
        end
    else
        ticky = [];
    end
    if iscell(y)
        displayseg = 1;
        for i = 1:l
            if l>1
                subplot(l,1,l-i+1,'align');
            end
            hold on
            if length(x)==1 && length(y)>1
                for k = 2:length(y)
                    x{k} = x{1};
                end
            end
            for j = 1:length(x)
                if length(x{j}) > 1
                    if size(x{j},1) == 1 && size(x{j},3) > 1
                        x{j} = reshape(x{j},size(x{j},2),size(x{j},3))';
                        mel = 1;
                    else
                        mel = 0;
                    end
                    xx = zeros(size(x{j},1)*size(y{j},4),1);
                    yy = zeros(size(y{j},1)*size(y{j},4),size(y{j},2));
                    for k = 1:size(y{j},4)
                        xx((k-1)*size(x{j},1)+1:k*size(x{j},1),1) = x{j}(:,1);
                        yy((k-1)*size(y{j},1)+1:k*size(y{j},1),:) = y{j}(:,:,i,k);
                    end
                    if size(y{j},4) == 1 && not(mel)
                        xxx = [1.5*xx(1,1)-0.5*xx(2,1);...
                                    (xx(1:end-1)+xx(2:end))/2;...
                                    1.5*xx(end,1)-0.5*xx(end-1,1)];
                    else
                        xxx = (0:size(yy,1))';
                    end
                    if size(fp{j},2) > 1
                        segt = [fp{j}(1,1),...
                                mean([fp{j}(2,1:end-1);fp{j}(1,2:end)]),...
                                fp{j}(2,end)];
                    else
                        segt = fp{j}';
                    end
                    surfplot(segt,xxx,yy(:,:));
                    if not(isempty(ticky))
                        set(gca,'ytick',ticky);
                        set(gca,'yticklabel',tickylab);
                    end
                    set(gca,'YDir','normal')
                    if (exist('pp') == 1) && length(pp)>=j
                        if not(isempty(pp{j}))
                            for k = 1:length(pp{j})
                                ppj = pp{j}{k};
                                if size(ppj,3) == 2
                                    ppj(:,:,1) = ppj(:,:,1) + (ppj(:,:,2)-1)*size(x,1);
                                    ppj(:,:,2) = [];
                                    plot(mean(fp{j}(:,k)),ppj-.5,'+w')
                                elseif not(isempty(ppj))
                                    plot(mean(fp{j}(:,k)),xx(ppj),'+k')
                                end
                            end
                        end
                    end
                end
            end
            if i == l
                title(t)
            end
            if i == 1
                if manychannels
                    xlabel(xlab);
                elseif displayseg
                    xlabel('time axis (in s.)');
                else
                    xlabel('temporal location of beginning of frame (in s.)');
                end
            end
            if l > 1
                num = ch(i);
                pos = get(gca,'Position');
                axes('Position',[pos(1)-.05 pos(2)+pos(4)/2 .01 .01],'Visible','off');
                text(0,0,num2str(num),'FontSize',12,'Color','r')
            end
        end
        y = y{1};
    else
        mel = 0;
        if iscell(x)
            x = x{1};
        end
        if size(x,1) == 1 && size(y,1) > 1
            if size(x,3) == 1
                x = x';
            else
                mel = 1;
            end
        end
        if not(isempty(pp)) && iscell(pp{1})
            pp = pp{1};
            pm = pm{1};
        end
        for i = 1:l
            if l>1
                subplot('Position',[0.1 (i-1)*il+0.1 0.89 il-0.02])
            end
            xx = zeros(size(x,1)*size(y,4),1);
            yy = zeros(size(y,1)*size(y,4),size(y,2));
            for k = 1:size(y,4)
                xx((k-1)*size(x,1)+1:k*size(x,1),1) = x(:,1);
                yy((k-1)*size(y,1)+1:k*size(y,1),:) = y(:,:,i,k);
            end
            if iscell(fp)
                fp = uncell(fp);
            end
            lx = length(xx);
            if lx < 6
                curve = 1;
                ttt = (fp(1,:)+fp(2,:))/2;
                plot(ttt,yy)
                if i == 1
                    legdata = cell(1,lx);
                    if strcmp(xlab(end),'s')
                        xlab(end) = [];
                    end
                    for j = 1:lx
                        legdata{j} = [xlab,' ',num2str(j)];
                    end
                    legend(legdata)
                end
            else
                ttt = [fp(1,:) 2*fp(1,end)-fp(1,end-1)];
                if size(y,4) == 1 && not(mel)
                    xxx = [1.5*xx(1)-0.5*xx(2);...
                           (xx(1:end-1)+xx(2:end))/2;...
                           1.5*xx(end)-0.5*xx(end-1)];
                else
                    xxx = (0:size(yy,1))';
                end
                %colormap('gray')
                surfplot(ttt,xxx,yy);
                if not(isempty(ticky))
                    set(gca,'ytick',ticky);
                    set(gca,'yticklabel',tickylab);
                end
                set(gca,'YDir','normal');
            end
            hold on

            if (exist('tp') == 1) && not(isempty(tp))
                tp = tp{i}{1};
                tv = tv{i}{1};
                for k = 1:size(tp,1)
                    prej = 0;
                    for j = 1:size(tp,2)
                        if ~isnan(tv(k,j)) && tv(k,j)
                            if prej && prej == j-1
                                if tp(k,j) == size(xxx,1) || ...
                                        tp(k,prej) == size(xxx,1)
                                    continue
                                end
                                plot([(ttt(prej)+ttt(prej+1))/2,...
                                      (ttt(j)+ttt(j+1))/2],...
                                     [(xxx(tp(k,prej))+xxx(tp(k,prej)+1))/2,...
                                      (xxx(tp(k,j))+xxx(tp(k,j)+1))/2],...
                                     'k','LineWidth',1)
                                plot([(ttt(prej)+ttt(prej+1))/2,...
                                      (ttt(j)+ttt(j+1))/2],...
                                     [(xxx(tp(k,prej))+xxx(tp(k,prej)+1))/2,...
                                      (xxx(tp(k,j))+xxx(tp(k,j)+1))/2],...
                                     'w+','MarkerSize',10)
                                plot([(ttt(prej)+ttt(prej+1))/2,...
                                      (ttt(j)+ttt(j+1))/2],...
                                     [(xxx(tp(k,prej))+xxx(tp(k,prej)+1))/2,...
                                      (xxx(tp(k,j))+xxx(tp(k,j)+1))/2],...
                                     'kx','MarkerSize',10)
                            end
                            prej = j;
                        end
                    end
                end
            elseif (exist('pp') == 1) && not(isempty(pp))
                if size(pp,3) == l
                    for j = 1:size(pp,2)
                        ppj = pp{1,j,i};
                        if (exist('pm') == 1)
                            pmj = pm{1,j,i};
                        else
                            pmj = [];
                        end
                        if iscell(ppj)
                            ppj = uncell(ppj);
                            if iscell(pmj)
                                pmj = uncell(pmj);
                            end
                        end
                        if size(ppj,3) == 2
                            ppj(:,:,1) = ppj(:,:,1) + (ppj(:,:,2)-1)*size(x,1);
                            ppj(:,:,2) = [];
                            %plot((fp(1,j)+fp(2,j))/2,ppj-.5,'+w')
                            plot((ttt(j)+ttt(j+1))/2,(xxx(ppj)+xxx(ppj+1))/2,'+w')
                        else
                            if not(isempty(pmj))
                                ppj(:,:,:) = ppj(:,:,:) + (pmj(:,:,:)-1)*size(x,1);
                            end
                            if not(isempty(ppj))
                                plot((ttt(j)+ttt(j+1))/2,(xxx(ppj)+xxx(ppj+1))/2,'+w','MarkerSize',10)
                            end
                        end
                    end
                else
                    for j = 1:size(pp,3)
                        ppj = pp{1,1,j};
                        if (exist('pm') == 1)
                            pmj = pm{1,1,j};
                        else
                            pmj = [];
                        end
                        if not(isempty(ppj))
                            plot((ttt(ppj)+ttt(ppj+1))/2,(xxx(j)+xxx(j+1))/2,'+k','MarkerSize',10)
                        end
                    end
                end
            end
            if i == l
                title(t)
            end
            if i == 1
                if manychannels
                    xlabel(xlab);
                else
                    xlabel('time axis (in s.)');
                end
            end
            if l > 1
                if iscell(x)
                    num = x{i}(1);
                else
                    num = ch(i);
                end
                pos = get(gca,'Position');
                hfig = axes('Position',[pos(1)-.05 pos(2)+pos(4)/2 .01 .01],'Visible','off');
                text(0,0,num2str(num),'FontSize',12,'Color','r')
            end
        end
    end
end
if l == 1
    if curve
        ylabel(ylab);
    elseif manychannels
        ylabel('Channels');
    else
        ylabel(xlab)
    end
end
res = 1;
drawnow