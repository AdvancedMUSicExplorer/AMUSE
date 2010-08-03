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
% Creation date: 16.06.2010
%
% Author: Igor Vatolkin
%
function matlabLDA( classificationArff, trainingArff, outputFile )
% Performs LDA classification
% 
% classificationArff is the path to ARFF file with data instances
%
% trainingArff is the path to ARFF file with labeled data instances
%
% outputFile is the path to output file

% Load the training data
[dataTr,attrTr]=arff_loader(trainingArff);

% Load the data for classification
[dataCl,attrCl]=arff_loader(classificationArff);

% Convert the data omitting Id and Category attributes (they may appear as
% the last two attributes)
% (a) training data
numberOfAttrsToOmitForTrainingSet = 0;
if(strcmp(strtrim(attrTr(1,length(attrTr)).name),'Category') || strcmp(strtrim(attrTr(1,length(attrTr)).name),'Id'))
    numberOfAttrsToOmitForTrainingSet = numberOfAttrsToOmitForTrainingSet + 1;
end;
if(strcmp(strtrim(attrTr(1,length(attrTr)-1).name),'Category') || strcmp(strtrim(attrTr(1,length(attrTr)-1).name),'Id'))
    numberOfAttrsToOmitForTrainingSet = numberOfAttrsToOmitForTrainingSet + 1;
end;
matrixTr=cell2mat(dataTr(:,1:length(dataTr)-numberOfAttrsToOmitForTrainingSet));

% (b) classification data
numberOfAttrsToOmitForClassifSet = 0;
if(strcmp(strtrim(attrCl(1,length(attrCl)).name),'Category') || strcmp(strtrim(attrCl(1,length(attrCl)).name),'Id'))
    numberOfAttrsToOmitForClassifSet = numberOfAttrsToOmitForClassifSet + 1;
end;
if(strcmp(strtrim(attrCl(1,length(attrCl)-1).name),'Category') || strcmp(strtrim(attrCl(1,length(attrCl)-1).name),'Id'))
    numberOfAttrsToOmitForClassifSet = numberOfAttrsToOmitForClassifSet + 1;
end;
matrixCl=cell2mat(dataCl(:,1:length(dataCl)-numberOfAttrsToOmitForClassifSet));

% Prepare the groups
group=dataTr{length(dataTr)};

% Classify
[cl,err] = classify(matrixCl,matrixTr,group);

% Save the results
file = fopen(outputFile,'w');
for i=1:size(cl)
    fprintf(file,'%s',cl{i});
    fprintf(file,'\n');
end
fclose(file);

exit



