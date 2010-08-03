function [data,attributes]=arff_loader(feature_file)
% arff_loader(feature_file) reads (multi)feature values from the Amuse ARFF 
% feature file.
%
% Created on 15.01.2007 by Igor Vatolkin
% Last changed on 19.06.2007 by Igor Vatolkin
% $Id: $

% Is the name of a feature file given?
if nargin < 1
   error('Not enough input arguments, please specify the feature file');
end

% Cell array for data
data={};

% Structure for attribute descriptions
attributes = struct('name',{},'type',{});

% Open the file with feature values
file = fopen(feature_file);

% Read the lines until EOF
while true
    tline = fgetl(file);
    
    % EOF
    if ~ischar(tline)
         break;
    end;
    
    % Skip empty line
    if isempty(tline)
        continue;
    end;
    
    % Skip comment line
    if tline(1) == '%' 
        continue;
    end;
    
    % If the beginning of data part has been found
    if strfind(upper(tline),'@DATA') == 1
        % Set the format for textscab (now we know how much attributes
        % exist..)
        format = '%n';
        for i=2:length(attributes)
            if strcmp(attributes(i).type,'NUMERIC')
                format=strcat(format,',%n');
            else
                format=strcat(format,',%s');
            end;
        end;
        % Read the data
        data=textscan(file,format,'treatAsEmpty', {'?'});
        break;
    % If an attribute description has been found
    elseif strfind(upper(tline),'@ATTRIBUTE') == 1
        % If the attribute name consists of one word
        if isempty(strfind(tline,''''))
            [token, remain] = strtok(tline); 
            [attributes(1,end+1).name, rem] = strtok(remain);
            [attributes(1,end).type, rem] = strtok(remain);
        % If the attribute name consists of several words bordered by
        % "'" - characters 
        else
            k=strfind(tline,'''');
            attributes(1,end+1).name = tline(k(1)+1:k(end)-1);
            attributes(1,end).type = strtrim(tline(k(end)+1:end));
        end;
    end;
end;

% Close the file with feature values
fclose(file);

% Check if data part has been found
if isempty(data{1,1})
    error('No data part found; given file is not a valid ARFF file.');
end;

% Fill in the matrix 
% attribute
%feat_values=zeros(length(attributes),length(data{1,1}));
%for i=1:(length(attributes))
%    feat_values(i,:)=data{1,i};
%end;

%%matrixTr=cell2mat(dataTr(:,1:230));
%%matrixCl = cell2mat(dataCl(:,1:230));
%%group=dataTr{232}
