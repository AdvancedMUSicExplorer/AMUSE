
%
% mirtoolbox_base.m
%%%%%%%%%%%%%%%%%%%
% This file is part of AMUSE framework (Advanced MUsic Explorer).
% 
% Copyright 2006-2012 by code authors
% 
% Created at TU Dortmund, Chair of Algorithm Engineering
% (Contact: <http://ls11-www.cs.tu-dortmund.de>) 
% 
% AMUSE is free software: you can redistribute it and/or modify
% it under the terms of the GNU Affero General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
%
% AMUSE is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
% GNU Affero General Public License for more details.
% 
% You should have received a copy of the GNU Affero General Public License
% along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
% 
% Created on 07.07.2007 by Igor Vatolkin
% Last modified on 19.08.2012 by Igor Vatolkin
% $Id: $
%%%%%%%%%%%%%%%%%%%%
% Extracts MIRToolbox features for a given wave file and saves results as AMUSE ARFF files
%
% Created on 07.07.2009 by Igor Vatolkin
% Last modified on 19.08.2012 by Igor Vatolkin
% $Id: $
function mirtoolbox_base(input_file,output_dir)

% Add the paths to AMUSE Matlab and MIR Toolbox libraries
addpath('/home/pingel/AMUSE/amuse/tools/MatlabFeatures');
addpath('/home/pingel/AMUSE/amuse/tools/MIRToolbox/MIRToolbox');
addpath('/home/pingel/AMUSE/amuse/tools/MIRToolbox/AuditoryToolbox');
% Do not use MIR Toolbox logging (otherway Matlab may stop due to the large number of log lines!)
mirverbose(0);

mirwaitbar(0);

% define window length of analysis window in number of samples
wlength = 512;
% define window offset in number of samples
woffset = wlength;

% create prefix for AMUSE ARFF feature file
p=strfind(input_file,'/');
k=strfind(input_file,'.');
output_file_prefix = [output_dir,'/',input_file(p(end)+1:k(end)-1),'_'];

	% Load the music data and calculate the frames
	a=miraudio(input_file);
	f=mirframe(a,'Length',wlength,'sp','Hop',woffset,'sp');

	% --------------------------------------------------------------------------------------------------
	% Features based on onset events -------------------------------------------------------------------
	% --------------------------------------------------------------------------------------------------
	%mirchunklim(2000000);
	onsets = mironsets(input_file,'Attacks','Releases');
	onsetsData = mirgetdata(onsets);	

		% Attack times
		mirres = get(onsets,'OnsetPosUnit');
		res = mirres{1}{1}{1};
		arff_saver([output_file_prefix,'423.arff'],'Start points of attack intervals',res,-1);
	
		% Rise times
		mirres=mirattacktime(onsets);
		res = mirgetdata(mirres);
		arff_saver([output_file_prefix,'428.arff'],'Rise times',res,-1);
	
		% Attack Slopes as windowed numeric
		% Create dummy file, that tells MIRToolboxAdapter that rise times need to be converted
		file = fopen([output_file_prefix,'430.arff'],'w');
		fprintf(file,['Rise times need to be converted']);
		fclose(file);
	
	exit
