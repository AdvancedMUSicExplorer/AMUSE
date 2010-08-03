function correlation(SONG_LIST,FEATURE_ID)
% hist(FILE_LIST,BIN_NUMBER,FEATURE_DESC) creates histograms
% and some statistical descriptors for one certain feature extracted
% from several example data sets saved as ARFF.
%
% FILE_LIST is a text file with a list of ARFF feature files. Every line 
% should contain the name of ARFF data example file and its short 
% description separated by a semicolon, e.g. 

%
% BIN_NUMBER is the number of histogram bins.
%
% FEATURE_DESC is the description of the feature.
% 

%
% Created on 14.06.2009 by Igor Vatolkin
% $Id: $ 

% is the name of parameters correct?
if nargin < 2
   error('Not enough input arguments.');
end

featuredatabase = '/home/vatol/Music/Features/CDs';

% calculate the dimensionality of the feature
file = fopen(SONG_LIST);
while true
    tline = fgetl(file);
    % EOF
	if ~ischar(tline)
		break;
	end;
    
   if strfind(upper(tline),'@DATA') == 1
       break;
   end;
end;
tline = fgetl(file);
c = strfind(tline,',');
tline = tline(c+1:length(tline));
p = strfind(tline,'/');
current_feature_file = [featuredatabase, '/', tline(1:(length(tline)-4)), '/', tline(p(end)+1:(length(tline)-4)), '_', num2str(FEATURE_ID), '.arff'];
features_for_current_file = arff_loader(current_feature_file);
feature_dimensions = length(features_for_current_file(:,1));
fclose(file);

featuremaxs = zeros(1,feature_dimensions);
featuremins = zeros(1,feature_dimensions);
for i=1:feature_dimensions
    featuremaxs(1,i) = -Inf;
    featuremins(1,i) = Inf;
end;

% proceed the music files one by one calculating the correlation between
% features
current_song = 1;
file = fopen(SONG_LIST);
while true
    tline = fgetl(file);
	
	% EOF
	if ~ischar(tline)
		break;
	end;
    if strfind(upper(tline),'@DATA') == 1
       break;
   end;
end;


while true
    tline = fgetl(file);
	
	% EOF
	if ~ischar(tline)
		break;
	end;    
    c = strfind(tline,',');
    tline = tline(c+1:length(tline));
    p = strfind(tline,'/');
    current_feature_file = [featuredatabase, '/', tline(1:(length(tline)-4)), '/', tline(p(end)+1:(length(tline)-4)), '_', num2str(FEATURE_ID), '.arff'];
    disp(['Analyzing: ',current_feature_file]);
    features_for_current_file = arff_loader(current_feature_file);
    
    % go through dimensions
    for i=1:feature_dimensions
        
        % go through feature values
        for j=1:length(features_for_current_file(1,:))
            
            % update maximum?
            if(features_for_current_file(i,j) > featuremaxs(1,i))
                featuremaxs(1,i) = features_for_current_file(i,j);
            end;
            
            % update minimum?
            if(features_for_current_file(i,j) < featuremins(1,i))
                featuremins(1,i) = features_for_current_file(i,j);
            end;
        end;
    end;
    
   
    current_song = current_song + 1;
end;
fclose(file);

% display the results
for i=1:feature_dimensions
    disp(['Feature dimension ', num2str(i), ': min: ', num2str(featuremins(1,i)), ' max: ', num2str(featuremaxs(1,i))]);
end;
